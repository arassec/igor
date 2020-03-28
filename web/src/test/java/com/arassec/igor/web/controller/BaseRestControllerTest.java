package com.arassec.igor.web.controller;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.web.model.JobExecutionListEntry;
import com.arassec.igor.web.model.KeyLabelStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.print.DocFlavor;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link BaseRestController}.
 */
@DisplayName("Base-Controller Tests")
public class BaseRestControllerTest {

    /**
     * The class under test.
     */
    private static final BaseRestController controller = mock(BaseRestController.class, CALLS_REAL_METHODS);

    /**
     * Tests sorting a key-label-store list by label.
     */
    @Test
    @DisplayName("Tests sorting a key-label-store list by label.")
    void testSortByLabel() {
        List<KeyLabelStore> sortedList = controller.sortByLabel(Set.of(new KeyLabelStore("keyB", "labelB"),
                new KeyLabelStore("keyC", "labelC"), new KeyLabelStore("keyA", "labelA")));
        assertEquals("keyA", sortedList.get(0).getKey());
        assertEquals("keyB", sortedList.get(1).getKey());
        assertEquals("keyC", sortedList.get(2).getKey());
    }

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
