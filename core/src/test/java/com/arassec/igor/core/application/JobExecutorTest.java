package com.arassec.igor.core.application;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.repository.JobExecutionRepository;
import com.arassec.igor.core.repository.JobRepository;
import com.arassec.igor.core.util.ModelPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link JobExecutor}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Job-Executor Tests")
public class JobExecutorTest {

    /**
     * The tested class.
     */
    private JobExecutor jobExecutor;

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
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        jobExecutor = new JobExecutor(jobRepository, jobExecutionRepository);
    }

    /**
     * Tests updating the job executor and by that removing job-executions of finished jobs.
     */
    @Test
    @DisplayName("Tests removing finished job executions.")
    void testUpdateRemoveFinished() throws ExecutionException, InterruptedException {
        // Save the "running jobs" map for later verification:
        Map<String, Job> runningJobs = new HashMap<>();
        ReflectionTestUtils.setField(jobExecutor, "runningJobs", runningJobs);

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
        ReflectionTestUtils.setField(jobExecutor, "runningJobFutures", runningJobFutures);
        runningJobs.put("running-job-id", runningJob);

        jobExecutor.update();

        // The finished job must have been removed:
        verify(jobExecutionRepository, times(1)).upsert(finishedJobExecution);
        assertFalse(runningJobs.containsKey("running-job-id"));
    }

    /**
     * Tests, that updating the job executor checks the configured number of job slots.
     */
    @Test
    @DisplayName("Tests queue size verification.")
    void testUpdateFullSlots() {
        // Configure the job slots to one:
        ReflectionTestUtils.setField(jobExecutor, "jobQueueSize", 1);

        // One job is currently executed:
        @SuppressWarnings("unchecked")
        Future<Job> finishedJobFuture = mock(Future.class);
        when(finishedJobFuture.isDone()).thenReturn(false);
        List<Future<Job>> runningJobFutures = new LinkedList<>();
        runningJobFutures.add(finishedJobFuture);

        ReflectionTestUtils.setField(jobExecutor, "runningJobFutures", runningJobFutures);
        jobExecutor.update();

        // The finished job must have been removed:
        verify(jobExecutionRepository, times(0)).findInState(eq(JobExecutionState.WAITING), eq(0), eq(Integer.MAX_VALUE));
    }

    /**
     * Tests executing a waiting job and delaying another one due to the configured job queue capacity.
     */
    @Test
    @DisplayName("Tests executing waiting jobs.")
    void testUpdateExecute() {
        // Configure the job slots to one, no jobs are currently running:
        ReflectionTestUtils.setField(jobExecutor, "jobQueueSize", 1);

        // First execution: No job found because it has been deleted this very moment:
        JobExecution ignoredExecution = JobExecution.builder().build();
        // Second execution: executed because the slot is free:
        JobExecution executedExecution = JobExecution.builder().jobId("job-id").build();
        Job job = new Job();
        job.setCurrentJobExecution(executedExecution);
        job.setId("job-id");
        lenient().when(jobRepository.findById(eq("job-id"))).thenReturn(job);
        // Third execution: delayed due to slot capacity:
        JobExecution delayedExecution = JobExecution.builder().build();

        when(jobExecutionRepository.findInState(eq(JobExecutionState.WAITING), eq(0), eq(Integer.MAX_VALUE)))
                .thenReturn(new ModelPage<>(0, 3, 1, List.of(ignoredExecution, executedExecution, delayedExecution)));

        jobExecutor.update();

        verify(jobExecutionRepository, times(0)).upsert(eq(ignoredExecution));
        verify(jobExecutionRepository, times(1)).upsert(eq(executedExecution));
        verify(jobExecutionRepository, times(0)).upsert(eq(delayedExecution));

        // The job might finished before the test checks the execution state. Thus both states are valid results!
        assertTrue((JobExecutionState.RUNNING.equals(executedExecution.getExecutionState())) ||
                (JobExecutionState.FINISHED.equals(executedExecution.getExecutionState())));
        assertNotNull(executedExecution.getStarted());
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
        // Makes the executor wait for the job to finish!
        AtomicInteger numInvocations = new AtomicInteger();
        assertFalse(doAnswer(invocation -> {
            numInvocations.getAndIncrement();
            return !(numInvocations.get() > 3);
        }).when(job).isRunning());

        ReflectionTestUtils.setField(jobExecutor, "runningJobs", Map.of("job-id", job));

        jobExecutor.cancel("job-id");

        verify(job, times(1)).cancel();
        assertFalse(verify(job, times(4)).isRunning());
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
        ReflectionTestUtils.setField(jobExecutor, "runningJobs", Map.of("job-id", job));

        assertNull(jobExecutor.getJobExecution("unknown-job-id"));
        assertEquals(jobExecution, jobExecutor.getJobExecution("job-id"));
    }

}
