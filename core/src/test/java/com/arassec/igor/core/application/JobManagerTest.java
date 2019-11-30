package com.arassec.igor.core.application;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.service.ServiceException;
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
import org.springframework.test.util.ReflectionTestUtils;

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
        ScheduledFuture scheduledFuture = mock(ScheduledFuture.class);
        Map<String, ScheduledFuture> scheduledJobs = new ConcurrentHashMap<>();
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

        ScheduledFuture scheduledFuture = mock(ScheduledFuture.class);
        Map<String, ScheduledFuture> scheduledJobs = new ConcurrentHashMap<>();
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

        ScheduledFuture scheduledFuture = mock(ScheduledFuture.class);
        Map<String, ScheduledFuture> scheduledJobs = new ConcurrentHashMap<>();
        scheduledJobs.put("job-id", scheduledFuture);
        ReflectionTestUtils.setField(jobManager, "scheduledJobs", scheduledJobs);

        when(jobRepository.upsert(eq(job))).thenReturn(job);
        when(scheduledFuture.cancel(eq(true))).thenReturn(true);

        Job savedJob = jobManager.save(job);

        assertEquals(job, savedJob);
        verify(scheduledFuture, times(1)).cancel(eq(true));
    }

}
