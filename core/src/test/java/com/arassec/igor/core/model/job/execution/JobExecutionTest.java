package com.arassec.igor.core.model.job.execution;

import com.arassec.igor.core.model.service.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link JobExecution} model.
 */
@DisplayName("Job-Execution Tests")
class JobExecutionTest {

    /**
     * Tests the cancellation of a job execution.
     */
    @Test
    @DisplayName("Tests cancelling a job-execution.")
    void testCancel() {
        JobExecution jobExecution = new JobExecution();
        assertNull(jobExecution.getExecutionState());
        assertNull(jobExecution.getFinished());

        jobExecution.cancel();

        assertEquals(JobExecutionState.CANCELLED, jobExecution.getExecutionState());
        assertNotNull(jobExecution.getFinished());
    }

    /**
     * Tests setting a job execution to 'failed'.
     */
    @Test
    @DisplayName("Tests setting a job-execution to 'failed'.'")
    void testFail() {
        JobExecution jobExecution = new JobExecution();
        assertNull(jobExecution.getExecutionState());
        assertNull(jobExecution.getFinished());
        assertNull(jobExecution.getErrorCause());

        jobExecution.fail(null);

        assertEquals(JobExecutionState.FAILED, jobExecution.getExecutionState());
        assertNotNull(jobExecution.getFinished());
        assertNull(jobExecution.getErrorCause());

        jobExecution = new JobExecution();

        jobExecution.fail(new ServiceException("test"));
        assertTrue(jobExecution.getErrorCause().startsWith("com.arassec.igor.core.model.service.ServiceException: test"));
    }

    /**
     * Tests, that the job-execution correctly determines whether it's running or not.
     */
    @Test
    @DisplayName("Tests the job-executions 'running' information.")
    void testRunning() {
        JobExecution jobExecution = new JobExecution();
        assertFalse(jobExecution.isRunning());

        jobExecution.setExecutionState(JobExecutionState.RUNNING);
        assertTrue(jobExecution.isRunning());

        jobExecution.setExecutionState(JobExecutionState.FINISHED);
        assertFalse(jobExecution.isRunning());
    }

    /**
     * Tests adding, removing and retrieving of work-in-progress information.
     */
    @Test
    @DisplayName("Tests work-in-progrss handling.")
    void testWorkInProgressHandling() {
        JobExecution jobExecution = new JobExecution();
        assertTrue(jobExecution.getWorkInProgress().isEmpty());

        WorkInProgressMonitor wipMon = new WorkInProgressMonitor("test-wip-mon", 123);

        jobExecution.addWorkInProgress(wipMon);
        assertEquals(wipMon, jobExecution.getWorkInProgress().get(0));

        jobExecution.removeWorkInProgress(wipMon);
        assertTrue(jobExecution.getWorkInProgress().isEmpty());
    }

}
