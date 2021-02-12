package com.arassec.igor.web.controller;

import com.arassec.igor.application.manager.JobManager;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.web.model.JobExecutionListEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link BaseRestController}.
 */
@DisplayName("Base-Controller Tests")
class BaseRestControllerTest {

    /**
     * The class under test.
     */
    private static final BaseRestController controller = mock(BaseRestController.class, CALLS_REAL_METHODS);

    /**
     * Tests converting a job execution into a list entry.
     */
    @Test
    @DisplayName("Tests converting a job execution into a list entry.")
    void testConvert() {
        assertNull(controller.convert(null, null));

        Instant created = Instant.now();
        Instant started = Instant.now();
        Instant finished = Instant.now();

        JobExecution execution = JobExecution.builder().id(123L).jobId("job-id").executionState(JobExecutionState.RUNNING)
                .created(created).started(started).build();
        JobExecutionListEntry result = controller.convert(execution, null);
        assertEquals(123L, result.getId());
        assertEquals("job-id", result.getJobId());
        assertEquals(JobExecutionState.RUNNING.name(), result.getState());
        assertNull(result.getJobName());
        assertEquals(created, result.getCreated());
        assertEquals(started, result.getStarted());
        assertFalse(result.getDuration().isBlank());

        execution = JobExecution.builder().id(123L).jobId("job-id").executionState(JobExecutionState.WAITING)
                .created(created).started(started).build();
        result = controller.convert(execution, "job-name");
        assertEquals("job-name", result.getJobName());
        assertFalse(result.getDuration().isBlank());

        execution = JobExecution.builder().id(123L).jobId("job-id").executionState(JobExecutionState.FAILED)
                .started(Instant.now()).finished(finished).build();
        result = controller.convert(execution, "job-name");
        assertEquals(finished, result.getFinished());

        execution = JobExecution.builder().id(123L).jobId("job-id").executionState(JobExecutionState.RESOLVED)
                .started(Instant.now()).build();
        result = controller.convert(execution, "job-name");
        assertEquals("", result.getDuration());
    }

    /**
     * Tests retrieving job executions.
     */
    @Test
    @DisplayName("Tests retrieving job executions.")
    void testDetermineJobExecutions() {
        Job job = Job.builder().id("job-id").name("job-name").currentJobExecution(
                JobExecution.builder().build()
        ).build();

        JobManager jobManagerMock = mock(JobManager.class);

        // If the supplied job provides an execution, that one must be taken:
        JobExecution jobExecution = controller.determineJobExecution(jobManagerMock, job);
        assertEquals(job.getCurrentJobExecution(), jobExecution);

        // If the supplied job doesn't provide an execution, the repository is queried for one:
        JobExecution persistedJobExecution = JobExecution.builder().build();
        when(jobManagerMock.getJobExecutionsOfJob("job-id", 0, 1)).thenReturn(
                new ModelPage<>(1, 1, 1, List.of(persistedJobExecution)));

        jobExecution = controller.determineJobExecution(jobManagerMock, Job.builder().id("job-id").build());
        assertEquals(persistedJobExecution, jobExecution);
    }


}
