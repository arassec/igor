package com.arassec.igor.starter.health;

import com.arassec.igor.application.manager.JobManager;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.util.ModelPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Teste the {@link JobsHealthIndicator}.
 */
@ExtendWith(MockitoExtension.class)
class JobsHealthIndicatorTest {

    /**
     * The health indicator under test.
     */
    @InjectMocks
    private JobsHealthIndicator jobsHealthIndicator;

    /**
     * The manager for jobs.
     */
    @Mock
    private JobManager jobManager;

    /**
     * Tests the health status if all jobs ran fine.
     */
    @Test
    @DisplayName("Tests the health status if all jobs ran fine.")
    void testHealthUp() {
        when(jobManager.getJobExecutionsInState(JobExecutionState.FAILED, 0, Integer.MAX_VALUE)).thenReturn(
            new ModelPage<>(0, 0, 1, List.of())
        );
        assertEquals(Status.UP, jobsHealthIndicator.health().getStatus());
    }

    /**
     * Tests the health status if a job execution failed.
     */
    @Test
    @DisplayName("Tests the health status if a job execution failed.")
    void testHealthDown() {
        when(jobManager.getJobExecutionsInState(JobExecutionState.FAILED, 0, Integer.MAX_VALUE)).thenReturn(
            new ModelPage<>(0, 2, 1, List.of(
                JobExecution.builder().jobId("job-id").build(),
                JobExecution.builder().jobId("job-id").build(), // Duplicates should be filtered
                JobExecution.builder().jobId("inactive-job-id").build() // Inactive jobs should be ignored
            ))
        );

        doReturn(Job.builder().active(true).name("job-name").build()).when(jobManager).load("job-id");
        doReturn(Job.builder().active(true).name("inactive-job-name").build()).when(jobManager).load("inactive-job-id");

        Health health = jobsHealthIndicator.health();

        verify(jobManager, times(1)).load("job-id");
        assertEquals(Status.DOWN, health.getStatus());

        @SuppressWarnings("unchecked")
        List<Map<String, ?>> details = (List<Map<String, ?>>) health.getDetails().get("jobs");
        assertEquals("job-id", details.get(0).get("jobId"));
        assertEquals("job-name", details.get(0).get("jobName"));
        assertEquals("FAILED", details.get(0).get("jobExecutionState"));
        assertEquals("inactive-job-id", details.get(1).get("jobId"));
        assertEquals("inactive-job-name", details.get(1).get("jobName"));
        assertEquals("FAILED", details.get(1).get("jobExecutionState"));
    }

}
