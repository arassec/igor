package com.arassec.igor.application.manager;

import com.arassec.igor.application.exec.JobExecutor;
import com.arassec.igor.core.IgorApplicationProperties;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.model.trigger.ScheduledTrigger;
import com.arassec.igor.core.repository.JobExecutionRepository;
import com.arassec.igor.core.repository.JobRepository;
import com.arassec.igor.core.repository.PersistentValueRepository;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.core.util.event.JobEvent;
import com.arassec.igor.core.util.event.JobEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link JobManager}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Job-Manager Tests")
class JobManagerTest {

    /**
     * The class under test.
     */
    private JobManager jobManager;

    /**
     * Mocked core configuration properties.
     */
    @Mock
    private IgorApplicationProperties igorCoreProperties;

    /**
     * Repository for jobs.
     */
    @Mock
    private JobRepository jobRepository;

    /**
     * Repository for job-executions.
     */
    @Mock
    private JobExecutionRepository jobExecutionRepository;

    /**
     * Repository for persistent values.
     */
    @Mock
    private PersistentValueRepository persistentValueRepository;

    /**
     * The task scheduler that starts the jobs according to their trigger.
     */
    @Mock
    private TaskScheduler taskScheduler;

    /**
     * The job executor actually running the scheduled jobs.
     */
    @Mock
    private JobExecutor jobExecutor;

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
        jobManager = new JobManager(igorCoreProperties, jobRepository, jobExecutionRepository,
                persistentValueRepository, taskScheduler, jobExecutor, applicationEventPublisher);
    }

    /**
     * Tests the initialization of the manager after the Spring-Context has been created.
     */
    @Test
    @DisplayName("Tests the initialization of the manager.")
    void testOnApplicationEvent() {
        jobManager.onApplicationEvent(mock(ContextRefreshedEvent.class));
        verify(jobExecutionRepository, times(0)).upsert(any(JobExecution.class));
        verify(jobRepository, times(1)).findAll();

        JobExecution runningJobExecution = new JobExecution();
        JobExecution activeJobExecution = new JobExecution();

        ModelPage<JobExecution> runningModelPage = new ModelPage<>(0, Integer.MAX_VALUE, 1, List.of(runningJobExecution));
        ModelPage<JobExecution> activeModelPage = new ModelPage<>(0, Integer.MAX_VALUE, 1, List.of(activeJobExecution));

        when(jobExecutionRepository.findInState(JobExecutionState.RUNNING, 0, Integer.MAX_VALUE)).thenReturn(runningModelPage);
        when(jobExecutionRepository.findInState(JobExecutionState.ACTIVE, 0, Integer.MAX_VALUE)).thenReturn(activeModelPage);

        jobManager.onApplicationEvent(mock(ContextRefreshedEvent.class));

        ArgumentCaptor<JobExecution> argCap = ArgumentCaptor.forClass(JobExecution.class);

        verify(jobExecutionRepository, times(2)).upsert(argCap.capture());

        assertEquals(JobExecutionState.FAILED, argCap.getAllValues().get(0).getExecutionState());
        assertNotNull(argCap.getAllValues().get(0).getFinished());
        assertEquals("Job interrupted due to application restart!", argCap.getAllValues().get(0).getErrorCause());
        assertEquals(JobExecutionState.FINISHED, argCap.getAllValues().get(1).getExecutionState());
        assertNotNull(argCap.getAllValues().get(1).getFinished());
        assertNull(argCap.getAllValues().get(1).getErrorCause());
    }

    /**
     * Tests the shutdown of the manager.
     */
    @Test
    @DisplayName("Tests the shutdown of the manager.")
    void testDestroy() {
        ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
        Map<String, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>();
        scheduledJobs.put("id", scheduledFuture);
        ReflectionTestUtils.setField(jobManager, "scheduledJobs", scheduledJobs);

        jobManager.destroy();

        verify(scheduledFuture, times(1)).cancel(true);
    }

    /**
     * Tests saving a job that is not active.
     */
    @Test
    @DisplayName("Tests saving an inactive job.")
    void testSaveInactiveJob() {
        Job job = new Job();
        job.setId("job-id");

        ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
        Map<String, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>();
        scheduledJobs.put("job-id", scheduledFuture);
        ReflectionTestUtils.setField(jobManager, "scheduledJobs", scheduledJobs);

        when(jobRepository.upsert(job)).thenReturn(job);

        assertThrows(IgorException.class, () -> jobManager.save(job));

        when(scheduledFuture.cancel(true)).thenReturn(true);

        Job savedJob = jobManager.save(job);

        assertEquals(job, savedJob);
        verify(scheduledFuture, times(2)).cancel(true);

        // A job event must be sent:
        ArgumentCaptor<JobEvent> argCap = ArgumentCaptor.forClass(JobEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(argCap.capture());
        assertEquals(JobEventType.SAVE, argCap.getValue().getType());
        assertEquals(savedJob, argCap.getValue().getJob());
    }

    /**
     * Tests saving an active job.
     */
    @Test
    @DisplayName("Tests saving an active job.")
    void testSaveActiveJob() {
        Job job = new Job();
        job.setId("job-id");
        job.setActive(true);

        ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
        Map<String, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>();
        scheduledJobs.put("job-id", scheduledFuture);
        ReflectionTestUtils.setField(jobManager, "scheduledJobs", scheduledJobs);

        when(jobRepository.upsert(job)).thenReturn(job);
        when(scheduledFuture.cancel(true)).thenReturn(true);

        Job savedJob = jobManager.save(job);

        assertEquals(job, savedJob);
        verify(scheduledFuture, times(1)).cancel(true);

        // A job event must be sent:
        ArgumentCaptor<JobEvent> argCap = ArgumentCaptor.forClass(JobEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(argCap.capture());
        assertEquals(JobEventType.SAVE, argCap.getValue().getType());
        assertEquals(savedJob, argCap.getValue().getJob());
    }

    /**
     * Tests deleting a scheduled job. This implicitly tests scheduling a job.
     */
    @Test
    @DisplayName("Tests deleting a scheduled job.")
    void testDeleteScheduledJob() {
        ScheduledTrigger scheduledTriggerMock = mock(ScheduledTrigger.class);
        when(scheduledTriggerMock.getCronExpression()).thenReturn("0 0 0 * * *");

        ScheduledFuture<?> scheduledFutureMock = mock(ScheduledFuture.class);
        when(scheduledFutureMock.cancel(true)).thenReturn(true);
        doReturn(scheduledFutureMock).when(taskScheduler).schedule(any(Runnable.class), any(CronTrigger.class));

        Job job = new Job();
        job.setId("job-id");
        job.setActive(true);
        job.setTrigger(scheduledTriggerMock);

        // This should schedule the active job:
        when(jobRepository.upsert(job)).thenReturn(job);
        jobManager.save(job);
        @SuppressWarnings("unchecked")
        Map<String, ScheduledFuture<?>> scheduledJobs = (Map<String, ScheduledFuture<?>>) ReflectionTestUtils.getField(jobManager, "scheduledJobs");
        assertTrue(scheduledJobs != null && !scheduledJobs.isEmpty());

        when(jobRepository.findById("job-id")).thenReturn(job);

        jobManager.delete("job-id");

        verify(jobExecutor, times(1)).cancel("job-id");
        verify(jobExecutionRepository, times(1)).deleteByJobId("job-id");
        verify(persistentValueRepository, times(1)).deleteByJobId("job-id");
        assertTrue(scheduledJobs.isEmpty());

        // A job event must be sent:
        ArgumentCaptor<JobEvent> argCap = ArgumentCaptor.forClass(JobEvent.class);
        verify(applicationEventPublisher, times(2)).publishEvent(argCap.capture());
        assertEquals(JobEventType.DELETE, argCap.getAllValues().get(1).getType());
        assertEquals(job, argCap.getAllValues().get(1).getJob());
    }

    /**
     * Tests enqueueing a new job.
     */
    @Test
    @DisplayName("Tests enqueueing a new job.")
    void testEnqueueNewJob() {
        Job job = new Job();

        assertThrows(IllegalArgumentException.class, () -> jobManager.enqueue(null));
        assertThrows(IllegalArgumentException.class, () -> jobManager.enqueue(job));

        job.setId("job-id");
        job.setHistoryLimit(666);

        jobManager.enqueue(job);

        ArgumentCaptor<JobExecution> argCap = ArgumentCaptor.forClass(JobExecution.class);

        verify(jobExecutionRepository, times(1)).upsert(argCap.capture());
        JobExecution jobExecution = argCap.getValue();
        assertEquals("job-id", jobExecution.getJobId());
        assertNotNull(jobExecution.getCreated());
        assertEquals(JobExecutionState.WAITING, jobExecution.getExecutionState());

        verify(jobExecutionRepository, times(1)).cleanup("job-id",666);

        // A job event must be sent:
        ArgumentCaptor<JobEvent> jobEventArgCap = ArgumentCaptor.forClass(JobEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(jobEventArgCap.capture());
        assertEquals(JobEventType.STATE_CHANGE, jobEventArgCap.getValue().getType());
        assertEquals(job, jobEventArgCap.getValue().getJob());
    }

    /**
     * Tests enqueueing a job while another execution of the same job is in progress.
     */
    @Test
    @DisplayName("Tests enqueueing an already running job.")
    void testEnqueueRunningJob() {
        Job job = Job.builder().id("job-id").historyLimit(666).build();

        // A running job blocks enqueueing the job again:
        reset(jobExecutionRepository);
        when(jobExecutionRepository.findAllOfJobInState("job-id", JobExecutionState.RUNNING)).thenReturn(List.of(new JobExecution()));

        jobManager.enqueue(job);

        verify(jobExecutionRepository, times(0)).upsert(any(JobExecution.class));

        // A waiting job also blocks enqueueing the job again:
        reset(jobExecutionRepository);
        when(jobExecutionRepository.findAllOfJobInState("job-id", JobExecutionState.RUNNING)).thenReturn(List.of());
        when(jobExecutionRepository.findAllOfJobInState("job-id", JobExecutionState.WAITING)).thenReturn(List.of(new JobExecution()));

        jobManager.enqueue(job);

        verify(jobExecutionRepository, times(0)).upsert(any(JobExecution.class));
        verify(applicationEventPublisher, times(0)).publishEvent(any(JobEvent.class));
    }

    /**
     * Tests enqueuing a fault intolerant job.
     */
    @Test
    @DisplayName("Tests enqueuing a fault intolerant job.")
    void testEnqueueFaultIntolerantJob() {
        Job job = Job.builder().id("job-id").faultTolerant(false).build();
        when(jobExecutionRepository.findAllOfJobInState("job-id", JobExecutionState.RUNNING)).thenReturn(List.of());
        when(jobExecutionRepository.findAllOfJobInState("job-id", JobExecutionState.WAITING)).thenReturn(List.of());
        when(jobExecutionRepository.findAllOfJobInState("job-id", JobExecutionState.FAILED)).thenReturn(List.of(new JobExecution()));
        jobManager.enqueue(job);
        verify(jobExecutionRepository, times(0)).upsert(any(JobExecution.class));
    }

    /**
     * Tests cancelling a job that doesn't exist or a job that is not in state {@link JobExecutionState#WAITING}.
     */
    @Test
    @DisplayName("Tests cancelling a job execution with an invalid state.")
    void testCancelExecutionInvalid() {
        assertThrows(IllegalArgumentException.class, () -> jobManager.cancelExecution(null));

        // Cancelling a non-existing job execution should silently be ignored:
        jobManager.cancelExecution(-1L);

        verify(jobExecutionRepository, times(0)).upsert(any(JobExecution.class));
        verify(jobExecutor, times(0)).cancel(any(String.class));

        // Cancelling a job in state "FINISHED":
        JobExecution jobExecutionMock = mock(JobExecution.class);
        when(jobExecutionMock.getExecutionState()).thenReturn(JobExecutionState.FINISHED);
        when(jobExecutionRepository.findById(666L)).thenReturn(jobExecutionMock);

        jobManager.cancelExecution(666L);

        verify(jobExecutionRepository, times(0)).upsert(any(JobExecution.class));
        verify(jobExecutor, times(0)).cancel(any(String.class));
    }

    /**
     * Tests cancelling a waiting job.
     */
    @Test
    @DisplayName("Tests cancelling a waiting job execution.")
    void testCancelExecutionWaiting() {
        JobExecution jobExecutionMock = mock(JobExecution.class);
        when(jobExecutionMock.getJobId()).thenReturn("job-id");
        when(jobExecutionMock.getExecutionState()).thenReturn(JobExecutionState.WAITING);
        when(jobExecutionRepository.findById(666L)).thenReturn(jobExecutionMock);

        Job job = Job.builder().build();
        when(jobRepository.findById("job-id")).thenReturn(job);

        jobManager.cancelExecution(666L);

        verify(jobExecutionRepository, times(1)).upsert(jobExecutionMock);
        verify(jobExecutionMock, times(1)).setExecutionState(JobExecutionState.CANCELLED);
        verify(jobExecutionMock, times(1)).setFinished(any(Instant.class));

        // A job event must be sent:
        ArgumentCaptor<JobEvent> argCap = ArgumentCaptor.forClass(JobEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(argCap.capture());
        assertEquals(JobEventType.STATE_CHANGE, argCap.getValue().getType());
        assertEquals(job, argCap.getValue().getJob());
    }

    /**
     * Tests cancelling a running job.
     */
    @Test
    @DisplayName("Tests cancelling a running job execution.")
    void testCancelExecutionRunning() {
        JobExecution jobExecutionMock = mock(JobExecution.class);
        when(jobExecutionMock.getJobId()).thenReturn("job-id");
        when(jobExecutionMock.getExecutionState()).thenReturn(JobExecutionState.RUNNING);
        when(jobExecutionRepository.findById(666L)).thenReturn(jobExecutionMock);

        jobManager.cancelExecution(666L);

        verify(jobExecutor, times(1)).cancel("job-id");
    }

    /**
     * Tests loading a job by its ID.
     */
    @Test
    @DisplayName("Tests loading a job by ID.")
    void testLoad() {
        Job job = new Job();
        when(jobRepository.findById("job-id")).thenReturn(job);
        assertEquals(job, jobManager.load("job-id"));
    }

    /**
     * Tests loading a job by its name.
     */
    @Test
    @DisplayName("Tests loading a job by name.")
    void testLoadByName() {
        Job job = new Job();
        when(jobRepository.findByName("job-name")).thenReturn(job);
        assertEquals(job, jobManager.loadByName("job-name"));
    }

    /**
     * Tests loading a page of jobs.
     */
    @Test
    @DisplayName("Tests loading a page of jobs.")
    void testLoadPage() {
        ModelPage<Job> jobModelPage = new ModelPage<>(0, 2, 1, List.of(Job.builder().build(), Job.builder().build()));
        when(jobRepository.findPage(0, Integer.MAX_VALUE, "job-name-filter")).thenReturn(jobModelPage);
        assertEquals(jobModelPage, jobManager.loadPage(0, 2, "job-name-filter", null));
    }

    /**
     * Tests loading a page of jobs with state filter.
     */
    @Test
    @DisplayName("Tests loading a page of jobs with state filter.")
    void testLoadPageWithStateFilter() {
        Job running = Job.builder().id("running").currentJobExecution(
                JobExecution.builder().executionState(JobExecutionState.RUNNING).build()).build();
        Job failed = Job.builder().id("failed").currentJobExecution(
                JobExecution.builder().executionState(JobExecutionState.FAILED).build()).build();
        Job runningHistory = Job.builder().id("runningHistory").build();
        Job failedHistory = Job.builder().id("failedHistory").build();

        ModelPage<Job> jobModelPage = new ModelPage<>(0, 4, 1, List.of(
                running, failed, runningHistory, failedHistory
        ));

        when(jobRepository.findPage(0, Integer.MAX_VALUE, "job-name-filter")).thenReturn(jobModelPage);

        lenient().when(jobExecutionRepository.countAllOfJobInState("failed", JobExecutionState.FAILED)).thenReturn(1);
        lenient().when(jobExecutionRepository.countAllOfJobInState("failedHistory", JobExecutionState.FAILED)).thenReturn(4);

        ModelPage<Job> resultPage = jobManager.loadPage(0, 2, "job-name-filter", Set.of(JobExecutionState.FAILED));

        assertEquals(0, resultPage.getNumber());
        assertEquals(2, resultPage.getSize());
        assertEquals(1, resultPage.getTotalPages());
        assertEquals(2, resultPage.getItems().size());
        assertEquals(failed, resultPage.getItems().get(0));
        assertEquals(failedHistory, resultPage.getItems().get(1));
    }

    /**
     * Tests loading scheduled jobs.
     */
    @Test
    @DisplayName("Tests loading all scheduled jobs.")
    void testLoadScheduled() {
        // This job should be filtered:
        Job inactiveJob = new Job();
        inactiveJob.setActive(false);

        // This job should be put to the end of the list:
        ScheduledTrigger laterTriggerMock = mock(ScheduledTrigger.class);
        when(laterTriggerMock.getCronExpression()).thenReturn("0 0 3 * * *");
        Job laterJob = new Job();
        laterJob.setActive(true);
        laterJob.setTrigger(laterTriggerMock);

        // This job should be the first entry of the result list:
        ScheduledTrigger earlierTriggerMock = mock(ScheduledTrigger.class);
        when(earlierTriggerMock.getCronExpression()).thenReturn("0 0 1 * * *");
        Job earlierJob = new Job();
        earlierJob.setActive(true);
        earlierJob.setTrigger(earlierTriggerMock);

        when(jobRepository.findAll()).thenReturn(List.of(inactiveJob, laterJob, earlierJob));

        List<Job> scheduledJobs = jobManager.loadScheduled();
        assertEquals(2, scheduledJobs.size());
        assertEquals(earlierJob, scheduledJobs.get(0));
        assertEquals(laterJob, scheduledJobs.get(1));
    }

    /**
     * Tests loading executions of a certain job.
     */
    @Test
    @DisplayName("Tests loading job executions of a job.")
    void testGetJobExecutionsOfJob() {
        ModelPage<JobExecution> jobExecutionModelPage = new ModelPage<>(1, 2, 3, List.of());
        when(jobExecutionRepository.findAllOfJob("job-id", 1, 2)).thenReturn(jobExecutionModelPage);
        assertEquals(jobExecutionModelPage, jobManager.getJobExecutionsOfJob("job-id", 1, 2));
    }

    /**
     * Tests loading a job execution by ID.
     */
    @Test
    @DisplayName("Tests loading a specific job execution.")
    void testGetJobExecution() {
        assertNull(jobManager.getJobExecution(-1L));

        // Finished job execution:
        JobExecution jobExecution = new JobExecution();
        jobExecution.setExecutionState(JobExecutionState.FINISHED);
        when(jobExecutionRepository.findById(1L)).thenReturn(jobExecution);

        assertEquals(jobExecution, jobManager.getJobExecution(1L));

        // Running job execution without executor information:
        jobExecution.setExecutionState(JobExecutionState.RUNNING);

        assertEquals(jobExecution, jobManager.getJobExecution(1L));

        // Running job execution with executor information:
        jobExecution.setJobId("job-id");

        JobExecution executorJobExecution = new JobExecution();
        executorJobExecution.addWorkInProgress(new WorkInProgressMonitor());
        when(jobExecutor.getJobExecution("job-id")).thenReturn(executorJobExecution);

        assertEquals(jobExecution, jobManager.getJobExecution(1L));
        assertEquals(executorJobExecution.getWorkInProgress(), jobExecution.getWorkInProgress());
    }

    /**
     * Loads a page of job executions in a certain state.
     */
    @Test
    @DisplayName("Tests loading job executions in state.")
    void testGetJobExecutionsInState() {
        assertThrows(IllegalArgumentException.class, () -> jobManager.getJobExecutionsInState(null, 1, 1), "JobExecutionState " +
                "required!");

        ModelPage<JobExecution> jobExecutionModelPage = new ModelPage<>(1, 2, 3, List.of());
        when(jobExecutionRepository.findInState(JobExecutionState.RUNNING, 1, 2)).thenReturn(jobExecutionModelPage);

        assertEquals(jobExecutionModelPage, jobManager.getJobExecutionsInState(JobExecutionState.RUNNING, 1, 2));
    }

    /**
     * Tests updating a job execution's state.
     */
    @Test
    @DisplayName("Tests updating a job execution's state.")
    void testUpdateJobExecutionState() {
        when(jobExecutionRepository.findById(666L)).thenReturn(JobExecution.builder().jobId("job-id").build());
        Job job = Job.builder().build();
        when(jobRepository.findById("job-id")).thenReturn(job);

        jobManager.updateJobExecutionState(666L, JobExecutionState.RESOLVED);

        verify(jobExecutionRepository, times(1)).updateJobExecutionState(666L, JobExecutionState.RESOLVED);

        // A job event must be sent:
        ArgumentCaptor<JobEvent> argCap = ArgumentCaptor.forClass(JobEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(argCap.capture());
        assertEquals(JobEventType.STATE_CHANGE, argCap.getValue().getType());
        assertEquals(job, argCap.getValue().getJob());
    }

    /**
     * Tests updating all job executions of a given job.
     */
    @Test
    @DisplayName("Tests bulk updating job executions.")
    void testUpdateAllJobExecutionsOfJob() {
        Job job = Job.builder().build();
        when(jobRepository.findById("job-id")).thenReturn(job);

        jobManager.updateAllJobExecutionsOfJob("job-id", JobExecutionState.FAILED, JobExecutionState.RESOLVED);

        verify(jobExecutionRepository, times(1)).updateAllJobExecutionsOfJob("job-id", JobExecutionState.FAILED,
                JobExecutionState.RESOLVED);

        // A job event must be sent:
        ArgumentCaptor<JobEvent> argCap = ArgumentCaptor.forClass(JobEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(argCap.capture());
        assertEquals(JobEventType.STATE_CHANGE, argCap.getValue().getType());
        assertEquals(job, argCap.getValue().getJob());
    }

    /**
     * Tests retrieving the number of execution slots for jobs.
     */
    @Test
    @DisplayName("Tests the retrieval of job execution slots.")
    void testGetNumSlots() {
        when(igorCoreProperties.getJobQueueSize()).thenReturn(23);
        assertEquals(23, jobManager.getNumSlots());
    }

    /**
     * Tests retrieving all connectors that a job with a given ID uses.
     */
    @Test
    @DisplayName("Tests finding connectors used by a job.")
    void testGetReferencedConnectors() {
        Set<Pair<String, String>> result = new HashSet<>();
        when(jobRepository.findReferencedConnectors("job-id")).thenReturn(result);
        assertEquals(result, jobManager.getReferencedConnectors("job-id"));
    }

    /**
     * Tests counting job executions.
     */
    @Test
    @DisplayName("Tests counting job executions.")
    void testCountJobExecutions() {
        when(jobExecutionRepository.countJobsWithState(JobExecutionState.FAILED)).thenReturn(666);
        assertEquals(666, jobManager.countJobExecutions(JobExecutionState.FAILED));
    }

    /**
     * Tests counting executions in a certain state of a certain job.
     */
    @Test
    @DisplayName("Tests counting executions in a certain state of a certain job.")
    void testCountExecutionsOfJobInState() {
        when(jobExecutionRepository.countAllOfJobInState("job-id", JobExecutionState.FAILED)).thenReturn(42);
        assertEquals(42, jobManager.countExecutionsOfJobInState("job-id", JobExecutionState.FAILED));
    }

    /**
     * Tests scheduling a job by calling {@link JobManager#onApplicationEvent} with different job configurations.
     */
    @Test
    @DisplayName("Tests scheduling a job.")
    void testSchedule() {
        Job job = mock(Job.class);
        when(job.getName()).thenReturn("job-name");
        when(jobRepository.findAll()).thenReturn(List.of(job));

        ContextRefreshedEvent contextRefreshedEventMock = mock(ContextRefreshedEvent.class);

        // Job without ID:
        assertThrows(IllegalArgumentException.class, () -> jobManager.onApplicationEvent(contextRefreshedEventMock),
                "A job without ID should throw an exception!");

        // Inactive Job:
        when(job.getId()).thenReturn("job-id");
        when(job.isActive()).thenReturn(false);

        jobManager.onApplicationEvent(mock(ContextRefreshedEvent.class));
        assertNull(verify(job, times(0)).getTrigger());

        // Invalid CRON expression:
        job = new Job();
        job.setId("job-id");
        job.setName("job-name");
        job.setActive(true);

        ScheduledTrigger scheduledTriggerMock = mock(ScheduledTrigger.class);
        lenient().when(scheduledTriggerMock.getCronExpression()).thenReturn("INVALID-CRON-EXPRESSION");
        job.setTrigger(scheduledTriggerMock);
        when(jobRepository.findAll()).thenReturn(List.of(job));

        assertThrows(IllegalStateException.class, () -> jobManager.onApplicationEvent(contextRefreshedEventMock),
                "Invalid CRON expression should trigger Exception!");

        // A valid job:
        ReflectionTestUtils.setField(jobManager, "scheduledJobs", new HashMap<>());
        lenient().when(scheduledTriggerMock.getCronExpression()).thenReturn("0 0 0 * * ?");
        jobManager.onApplicationEvent(mock(ContextRefreshedEvent.class));
        verify(taskScheduler, times(1)).schedule(any(Runnable.class), any(CronTrigger.class));
    }

}
