package com.arassec.igor.application.execution;

import com.arassec.igor.application.IgorApplicationProperties;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.trigger.EventTrigger;
import com.arassec.igor.core.model.trigger.EventType;
import com.arassec.igor.core.repository.JobExecutionRepository;
import com.arassec.igor.core.repository.JobRepository;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.event.JobEvent;
import com.arassec.igor.core.util.event.JobEventType;
import com.arassec.igor.core.util.event.JobTriggerEvent;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link JobExecutor}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Job-Executor Tests")
class JobExecutorTest {

    /**
     * The tested class.
     */
    private JobExecutor jobExecutor;

    /**
     * Mocked application configuration properties.
     */
    @Mock
    private IgorApplicationProperties igorApplicationProperties;

    /**
     * Mocked repository for jobs.
     */
    @Mock
    private JobRepository jobRepository;

    /**
     * Mocked repository for job-executions.
     */
    @Mock
    private JobExecutionRepository jobExecutionRepository;

    /**
     * Publisher for events based on job changes.
     */
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        when(igorApplicationProperties.getJobQueueSize()).thenReturn(1);
        jobExecutor = new JobExecutor(igorApplicationProperties, jobRepository, jobExecutionRepository, applicationEventPublisher);
    }

    /**
     * Tests updating the job executor and by that removing job-executions of finished jobs.
     */
    @Test
    @DisplayName("Tests removing finished job executions.")
    void testUpdateRemoveFinished() throws ExecutionException, InterruptedException {
        // Save the "running jobs" map for later verification:
        Map<String, Job> runningJobs = new HashMap<>();
        ReflectionTestUtils.setField(jobExecutor, "currentlyProcessedJobs", runningJobs);

        // Configuration of the finished job:
        JobExecution finishedJobExecution = JobExecution.builder().build();
        Job runningJob = new Job();
        runningJob.setCurrentJobExecution(finishedJobExecution);
        runningJob.setId("running-job-id");
        @SuppressWarnings("unchecked")
        Future<Job> finishedJobFuture = mock(Future.class);
        when(finishedJobFuture.isDone()).thenReturn(true);
        when(finishedJobFuture.get()).thenReturn(runningJob);
        List<Future<Job>> runningJobFutures = new LinkedList<>();
        runningJobFutures.add(finishedJobFuture);
        ReflectionTestUtils.setField(jobExecutor, "currentlyProcessedJobFutures", runningJobFutures);
        runningJobs.put("running-job-id", runningJob);

        when(jobExecutionRepository
                .findInState(JobExecutionState.WAITING, 0, Integer.MAX_VALUE))
                .thenReturn(new ModelPage<>(1, 0, 1, List.of()));

        jobExecutor.update();

        // The finished job must have been removed:
        verify(jobExecutionRepository, times(1)).upsert(finishedJobExecution);
        assertFalse(runningJobs.containsKey("running-job-id"));

        // An update event must be sent:
        ArgumentCaptor<JobEvent> argCap = ArgumentCaptor.forClass(JobEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(argCap.capture());
        assertEquals(JobEventType.STATE_CHANGE, argCap.getValue().type());
        assertEquals(runningJob, argCap.getValue().job());
    }

    /**
     * Tests, that updating the job executor checks the configured number of job slots.
     */
    @Test
    @DisplayName("Tests queue size verification.")
    void testUpdateFullSlots() {
        // One job is currently executed:
        @SuppressWarnings("unchecked")
        Future<Job> finishedJobFuture = mock(Future.class);
        when(finishedJobFuture.isDone()).thenReturn(false);
        List<Future<Job>> runningJobFutures = new LinkedList<>();
        runningJobFutures.add(finishedJobFuture);

        Map<String, Job> runningJobs = new HashMap<>();
        Job runningJob = Job.builder().id("running-job-id").name("running-job-name").build();
        runningJobs.put("running-job-id", runningJob);

        ReflectionTestUtils.setField(jobExecutor, "currentlyProcessedJobFutures", runningJobFutures);
        ReflectionTestUtils.setField(jobExecutor, "currentlyProcessedJobs", runningJobs);

        // Another job is waiting for execution:
        when(jobExecutionRepository.findInState(JobExecutionState.WAITING, 0, Integer.MAX_VALUE)).thenReturn(
                new ModelPage<>(0, 1, 1, List.of(JobExecution.builder().id(1L).jobId("job-id").build()))
        );
        Job waitingJob = Job.builder().id("waiting-job-id").name("waiting-job-name").build();
        when(jobRepository.findById("job-id")).thenReturn(waitingJob);

        // update has to check the number of available slots;
        jobExecutor.update();

        verify(jobExecutionRepository, times(0)).upsert(any(JobExecution.class));

        // Update events must be sent for both jobs, running and waiting:
        ArgumentCaptor<JobEvent> argCap = ArgumentCaptor.forClass(JobEvent.class);
        verify(applicationEventPublisher, times(2)).publishEvent(argCap.capture());

        assertEquals(JobEventType.STATE_REFRESH, argCap.getAllValues().getFirst().type());
        assertEquals(runningJob, argCap.getAllValues().getFirst().job());
        assertEquals(JobEventType.STATE_REFRESH, argCap.getAllValues().get(1).type());
        assertEquals(waitingJob, argCap.getAllValues().get(1).job());
    }

    /**
     * Tests executing a waiting job and delaying another one due to the configured job queue capacity.
     */
    @Test
    @DisplayName("Tests executing waiting jobs.")
    void testUpdateExecute() {
        // First execution: No job found because it has been deleted this very moment:
        JobExecution ignoredExecution = JobExecution.builder().build();
        // Second execution: executed because the slot is free:
        JobExecution executedExecution = JobExecution.builder().jobId("job-id").build();
        Job job = new Job();
        job.setCurrentJobExecution(executedExecution);
        job.setId("job-id");
        lenient().when(jobRepository.findById("job-id")).thenReturn(job);
        // Third execution: delayed due to slot capacity:
        JobExecution delayedExecution = JobExecution.builder().build();

        when(jobExecutionRepository.findInState(JobExecutionState.WAITING, 0, Integer.MAX_VALUE))
                .thenReturn(new ModelPage<>(0, 3, 1, List.of(ignoredExecution, executedExecution, delayedExecution)));

        jobExecutor.update();

        verify(jobExecutionRepository, times(0)).upsert(ignoredExecution);
        verify(jobExecutionRepository, times(1)).upsert(executedExecution);
        verify(jobExecutionRepository, times(0)).upsert(delayedExecution);

        // The job might finished before the test checks the execution state. Thus both states are valid results!
        assertTrue((JobExecutionState.RUNNING.equals(executedExecution.getExecutionState())) ||
                (JobExecutionState.FINISHED.equals(executedExecution.getExecutionState())));
        assertNotNull(executedExecution.getStarted());

        // An update event must be sent:
        ArgumentCaptor<JobEvent> argCap = ArgumentCaptor.forClass(JobEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(argCap.capture());
        assertEquals(JobEventType.STATE_CHANGE, argCap.getValue().type());
        assertEquals(job, argCap.getValue().job());
    }

    /**
     * Tests updating an active job.
     */
    @Test
    @DisplayName("Tests updating an active job.")
    void testUpdateActiveJob() {
        // Save the "running jobs" map for later verification:
        Map<String, Job> runningJobs = new HashMap<>();
        ReflectionTestUtils.setField(jobExecutor, "currentlyProcessedJobs", runningJobs);

        JobExecution jobExecution = JobExecution.builder().jobId("job-id").build();

        when(jobExecutionRepository.findInState(JobExecutionState.WAITING, 0,
                Integer.MAX_VALUE)).thenReturn(new ModelPage<>(0, 1, 1,
                List.of(jobExecution)));

        EventTrigger eventTriggerMock = mock(EventTrigger.class);

        when(jobRepository.findById("job-id")).thenReturn(
                Job.builder().trigger(eventTriggerMock).build());

        jobExecutor.update();

        assertEquals(JobExecutionState.ACTIVE, jobExecution.getExecutionState());
    }

    /**
     * Tests cancelling a job with a given ID.
     */
    @Test
    @DisplayName("Tests cancelling a job.")
    void testCancel() {
        assertThrows(IllegalArgumentException.class, () -> jobExecutor.cancel(null), "Cannot cancel a job without a job ID!");

        // Nothing should happen with an empty "running jobs" list:
        jobExecutor.cancel("unknown-job-id");

        Job job = mock(Job.class);

        ReflectionTestUtils.setField(jobExecutor, "currentlyProcessedJobs", Map.of("job-id", job));

        jobExecutor.cancel("job-id");

        verify(job, times(1)).cancel();

        // An update event must be sent:
        ArgumentCaptor<JobEvent> argCap = ArgumentCaptor.forClass(JobEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(argCap.capture());
        assertEquals(JobEventType.STATE_CHANGE, argCap.getValue().type());
        assertEquals(job, argCap.getValue().job());
    }

    /**
     * Tests getting a job execution.
     */
    @Test
    @DisplayName("Tests getting a job execution.")
    void testGetJobExecution() {
        Job job = new Job();
        JobExecution jobExecution = new JobExecution();
        job.setCurrentJobExecution(jobExecution);
        ReflectionTestUtils.setField(jobExecutor, "currentlyProcessedJobs", Map.of("job-id", job));

        assertNull(jobExecutor.getJobExecution("unknown-job-id"));
        assertEquals(jobExecution, jobExecutor.getJobExecution("job-id"));
    }

    /**
     * Tests a fault-tolerant job.
     */
    @Test
    @DisplayName("Tests a fault-tolerant job.")
    @SneakyThrows
    void testFaultTolerantJob() {
        Job job = new Job();
        job.setId("job-id");
        job.setFaultTolerant(true);

        JobExecution jobExecution = new JobExecution();
        jobExecution.setExecutionState(JobExecutionState.FINISHED);

        job.setCurrentJobExecution(jobExecution);

        Map<String, Job> runningJobs = new HashMap<>();
        runningJobs.put("job-id", job);

        @SuppressWarnings("unchecked")
        Future<Job> jobFuture = mock(Future.class);
        when(jobFuture.isDone()).thenReturn(true);
        when(jobFuture.get()).thenReturn(job);

        List<Future<Job>> runningJobFutures = new LinkedList<>();
        runningJobFutures.add(jobFuture);

        ReflectionTestUtils.setField(jobExecutor, "currentlyProcessedJobs", runningJobs);
        ReflectionTestUtils.setField(jobExecutor, "currentlyProcessedJobFutures", runningJobFutures);

        when(jobExecutionRepository
                .findInState(JobExecutionState.WAITING, 0, Integer.MAX_VALUE))
                .thenReturn(new ModelPage<>(1, 0, 1, List.of()));

        jobExecutor.update();

        verify(jobExecutionRepository, times(1)).updateAllJobExecutionsOfJob("job-id",
                JobExecutionState.FAILED, JobExecutionState.RESOLVED);
    }

    /**
     * Tests processing of Job-Trigger events.
     */
    @Test
    @DisplayName("Tests processing of Job-Trigger events.")
    void testOnJobTriggerEvent() {
        EventTrigger eventTriggerMock = mock(EventTrigger.class);
        when(eventTriggerMock.getSupportedEventType()).thenReturn(EventType.WEB_HOOK);

        Job job = Job.builder().id("job-id").trigger(eventTriggerMock).build();

        ReflectionTestUtils.setField(jobExecutor, "currentlyProcessedJobs", Map.of("job-id", job));

        JobTriggerEvent jobTriggerEvent = new JobTriggerEvent("job-id", Map.of("a", 1), EventType.WEB_HOOK);

        jobExecutor.onJobTriggerEvent(jobTriggerEvent);

        verify(eventTriggerMock, times(1)).processEvent(jobTriggerEvent.eventData());
    }

    /**
     * Tests error handling while finishing a job.
     */
    @Test
    @DisplayName("Tests error handling while finishing a job.")
    @SneakyThrows
    @SuppressWarnings("unchecked")
    void testProcessFinishedErrorHandling() {
        when(jobExecutionRepository.findInState(JobExecutionState.WAITING, 0, Integer.MAX_VALUE))
                .thenReturn(new ModelPage<>(1,0,1, List.of()));

        // Test InterruptedException:
        Future<Job> futureMock = mock(Future.class);
        when(futureMock.isDone()).thenReturn(true);
        when(futureMock.get()).thenThrow(new InterruptedException("Test-Exception"));

        ReflectionTestUtils.setField(jobExecutor, "currentlyProcessedJobFutures", new ArrayList<>(List.of(futureMock)));

        assertDoesNotThrow(() -> jobExecutor.update());

        // Test ExecutionException:
        futureMock = mock(Future.class);
        when(futureMock.isDone()).thenReturn(true);
        when(futureMock.get()).thenThrow(new ExecutionException("Test-Exception", new Exception()));

        ReflectionTestUtils.setField(jobExecutor, "currentlyProcessedJobFutures", new ArrayList<>(List.of(futureMock)));

        assertDoesNotThrow(() -> jobExecutor.update());
    }

}
