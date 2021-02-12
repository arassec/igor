package com.arassec.igor.application.exec;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link JobRunningCallable}.
 */
@DisplayName("JobRunning-Callable Tests")
class JobRunningCallableTest {

    /**
     * Tests actually calling the supplied job.
     */
    @Test
    @DisplayName("Tests calling the job.")
    void testCall() {
        Job jobMock = mock(Job.class);
        JobExecution jobExecution = new JobExecution();

        JobRunningCallable jobRunningCallable = new JobRunningCallable(jobMock, jobExecution);

        Job calledJob = jobRunningCallable.call();

        assertEquals(jobMock, calledJob);
        verify(jobMock, times(1)).start(jobExecution);
    }

}
