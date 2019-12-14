package com.arassec.igor.core.application;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.core.model.trigger.ScheduledTrigger;
import com.arassec.igor.core.repository.JobExecutionRepository;
import com.arassec.igor.core.repository.JobRepository;
import com.arassec.igor.core.repository.PersistentValueRepository;
import com.arassec.igor.core.util.ModelPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
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
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        jobManager = new JobManager(jobRepository, jobExecutionRepository, persistentValueRepository, taskScheduler, jobExecutor);
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

        JobExecution firstJobExecution = new JobExecution();
        JobExecution secondJobExecution = new JobExecution();

        ModelPage<JobExecution> modelPage = new ModelPage<>(0, Integer.MAX_VALUE, 1, List.of(firstJobExecution, secondJobExecution));

        when(jobExecutionRepository.findInState(eq(JobExecutionState.RUNNING), eq(0), eq(Integer.MAX_VALUE))).thenReturn(modelPage);

        jobManager.onApplicationEvent(mock(ContextRefreshedEvent.class));

        ArgumentCaptor<JobExecution> argCap = ArgumentCaptor.forClass(JobExecution.class);

        verify(jobExecutionRepository, times(2)).upsert(argCap.capture());

        assertEquals(JobExecutionState.FAILED, argCap.getAllValues().get(0).getExecutionState());
        assertNotNull(argCap.getAllValues().get(0).getFinished());
        assertEquals("Job interrupted due to application restart!", argCap.getAllValues().get(0).getErrorCause());
        assertEquals(JobExecutionState.FAILED, argCap.getAllValues().get(1).getExecutionState());
        assertNotNull(argCap.getAllValues().get(1).getFinished());
        assertEquals("Job interrupted due to application restart!", argCap.getAllValues().get(1).getErrorCause());
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

        verify(scheduledFuture, times(1)).cancel(eq(true));
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

        when(jobRepository.upsert(eq(job))).thenReturn(job);

        assertThrows(ServiceException.class, () -> jobManager.save(job));

        when(scheduledFuture.cancel(eq(true))).thenReturn(true);

        Job savedJob = jobManager.save(job);

        assertEquals(job, savedJob);
        verify(scheduledFuture, times(2)).cancel(eq(true));
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

        when(jobRepository.upsert(eq(job))).thenReturn(job);
        when(scheduledFuture.cancel(eq(true))).thenReturn(true);

        Job savedJob = jobManager.save(job);

        assertEquals(job, savedJob);
        verify(scheduledFuture, times(1)).cancel(eq(true));
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
        when(scheduledFutureMock.cancel(eq(true))).thenReturn(true);
        doReturn(scheduledFutureMock).when(taskScheduler).schedule(any(Runnable.class), any(CronTrigger.class));

        Job job = new Job();
        job.setId("job-id");
        job.setActive(true);
        job.setTrigger(scheduledTriggerMock);

        // This should schedule the active job:
        when(jobRepository.upsert(eq(job))).thenReturn(job);
        jobManager.save(job);
        @SuppressWarnings("unchecked")
        Map<String, ScheduledFuture<?>> scheduledJobs = (Map<String, ScheduledFuture<?>>) ReflectionTestUtils.getField(jobManager, "scheduledJobs");
        assertTrue(scheduledJobs != null && !scheduledJobs.isEmpty());

        when(jobRepository.findById(eq("job-id"))).thenReturn(job);

        jobManager.delete("job-id");

        verify(jobExecutor, times(1)).cancel(eq("job-id"));
        verify(jobExecutionRepository, times(1)).deleteByJobId(eq("job-id"));
        verify(persistentValueRepository, times(1)).deleteByJobId(eq("job-id"));
        assertTrue(scheduledJobs.isEmpty());
    }

    /**
     * Tests enqueueing a new job.
     */
    @Test
    @DisplayName("Tests enqueueing a new job.")
    void testEnqueueNewJob() {
        assertThrows(IllegalArgumentException.class, () -> jobManager.enqueue(null), "Job with ID required for run!");
        assertThrows(IllegalArgumentException.class, () -> jobManager.enqueue(new Job()), "Job with ID required for run!");

        Job job = new Job();
        job.setId("job-id");
        job.setExecutionHistoryLimit(666);

        jobManager.enqueue(job);

        ArgumentCaptor<JobExecution> argCap = ArgumentCaptor.forClass(JobExecution.class);

        verify(jobExecutionRepository, times(1)).upsert(argCap.capture());
        JobExecution jobExecution = argCap.getValue();
        assertEquals("job-id", jobExecution.getJobId());
        assertNotNull(jobExecution.getCreated());
        assertEquals(JobExecutionState.WAITING, jobExecution.getExecutionState());

        verify(jobExecutionRepository, times(1)).cleanup(eq("job-id"), eq(666));
    }

    /**
     * Tests enqueueing a job while another execution of the same job is in progress.
     */
    @Test
    @DisplayName("Tests enqueueing an already running job.")
    void testEnqueueRunningJob() {
        Job job = new Job();
        job.setId("job-id");
        job.setExecutionHistoryLimit(666);

        // A running job blocks enqueueing the job again:
        reset(jobExecutionRepository);
        when(jobExecutionRepository.findAllOfJobInState(eq("job-id"), eq(JobExecutionState.RUNNING))).thenReturn(List.of(new JobExecution()));

        jobManager.enqueue(job);

        verify(jobExecutionRepository, times(0)).upsert(any(JobExecution.class));

        // A waiting job also blocks enqueueing the job again:
        reset(jobExecutionRepository);
        when(jobExecutionRepository.findAllOfJobInState(eq("job-id"), eq(JobExecutionState.RUNNING))).thenReturn(List.of());
        when(jobExecutionRepository.findAllOfJobInState(eq("job-id"), eq(JobExecutionState.WAITING))).thenReturn(List.of(new JobExecution()));

        jobManager.enqueue(job);

        verify(jobExecutionRepository, times(0)).upsert(any(JobExecution.class));
    }

    /**
     * Tests cancelling a job that doesn't exist or a job that is not in state {@link JobExecutionState#WAITING}.
     */
    @Test
    @DisplayName("Tests cancelling a job with an invalid state.")
    void testCancelInvalid() {
        assertThrows(IllegalArgumentException.class, () -> jobManager.cancel(null), "ID required to cancel job-execution!");

        // Cancelling a non-existing job execution should silently be ignored:
        jobManager.cancel(-1L);

        verify(jobExecutionRepository, times(0)).upsert(any(JobExecution.class));
        verify(jobExecutor, times(0)).cancel(any(String.class));

        // Cancelling a job in state "FINISHED":
        JobExecution jobExecutionMock = mock(JobExecution.class);
        when(jobExecutionMock.getExecutionState()).thenReturn(JobExecutionState.FINISHED);
        when(jobExecutionRepository.findById(eq(666L))).thenReturn(jobExecutionMock);

        jobManager.cancel(666L);

        verify(jobExecutionRepository, times(0)).upsert(any(JobExecution.class));
        verify(jobExecutor, times(0)).cancel(any(String.class));
    }

    /**
     * Tests cancelling a waiting job.
     */
    @Test
    @DisplayName("Tests cancelling a waiting job.")
    void testCancelWaiting() {
        JobExecution jobExecutionMock = mock(JobExecution.class);
        when(jobExecutionMock.getExecutionState()).thenReturn(JobExecutionState.WAITING);
        when(jobExecutionRepository.findById(eq(666L))).thenReturn(jobExecutionMock);

        jobManager.cancel(666L);

        verify(jobExecutionRepository, times(1)).upsert(eq(jobExecutionMock));
        verify(jobExecutionMock, times(1)).setExecutionState(eq(JobExecutionState.CANCELLED));
        verify(jobExecutionMock, times(1)).setFinished(any(Instant.class));
    }

    /**
     * Tests cancelling a running job.
     */
    @Test
    @DisplayName("Tests cancelling a running job.")
    void testCancelRunning() {
        JobExecution jobExecutionMock = mock(JobExecution.class);
        when(jobExecutionMock.getJobId()).thenReturn("job-id");
        when(jobExecutionMock.getExecutionState()).thenReturn(JobExecutionState.RUNNING);
        when(jobExecutionRepository.findById(eq(666L))).thenReturn(jobExecutionMock);

        jobManager.cancel(666L);

        verify(jobExecutor, times(1)).cancel(eq("job-id"));
    }

    /**
     * Tests loading a job by its ID.
     */
    @Test
    @DisplayName("Tests loading a job by ID.")
    void testLoad() {
        Job job = new Job();
        when(jobRepository.findById(eq("job-id"))).thenReturn(job);
        assertEquals(job, jobManager.load("job-id"));
    }

    /**
     * Tests loading a job by its name.
     */
    @Test
    @DisplayName("Tests loading a job by name.")
    void testLoadByName() {
        Job job = new Job();
        when(jobRepository.findByName(eq("job-name"))).thenReturn(job);
        assertEquals(job, jobManager.loadByName("job-name"));
    }

    /**
     * Tests loading a page of jobs.
     */
    @Test
    @DisplayName("Tests loading a page of jobs.")
    void testLoadPage() {
        ModelPage<Job> jobModelPage = new ModelPage<>(1, 2, 3, List.of());
        when(jobRepository.findPage(eq(1), eq(2), eq("job-name-filter"))).thenReturn(jobModelPage);
        assertEquals(jobModelPage, jobManager.loadPage(1, 2, "job-name-filter"));
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
        when(jobExecutionRepository.findAllOfJob(eq("job-id"), eq(1), eq(2))).thenReturn(jobExecutionModelPage);
        assertEquals(jobExecutionModelPage, jobManager.getJobExecutionsOfJob("job-id", 1, 2));
    }

}
