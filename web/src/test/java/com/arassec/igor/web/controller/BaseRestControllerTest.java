package com.arassec.igor.web.controller;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.web.model.JobExecutionListEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

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

}
