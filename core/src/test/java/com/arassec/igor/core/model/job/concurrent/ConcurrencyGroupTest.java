package com.arassec.igor.core.model.job.concurrent;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.util.IgorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ConcurrencyGroup}.
 */
@DisplayName("Concurrency-Group Tests")
class ConcurrencyGroupTest {

    /**
     * Tests a concurrency-group's complete lifecycle from creation to shutdown.
     *
     * @throws InterruptedException In case of an interruption during the test.
     */
    @Test
    @DisplayName("Tests the concurrency-group lifecylce.")
    void testLifecycle() throws InterruptedException {
        Action actionMock = mock(Action.class);
        when(actionMock.getNumThreads()).thenReturn(1);
        when(actionMock.process(anyMap(), nullable(JobExecution.class))).thenAnswer(invocationOnMock -> {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = List.of((Map<String, Object>) invocationOnMock.getArgument(0));
            return result;
        });

        BlockingQueue<Map<String, Object>> inputQueue = new LinkedBlockingQueue<>();

        ConcurrencyGroup concurrencyGroup = new ConcurrencyGroup(List.of(actionMock), inputQueue, "group-id", null);

        Map<String, Object> data = new HashMap<>();
        data.put("foo", "bar");

        inputQueue.add(data);

        concurrencyGroup.complete();
        concurrencyGroup.shutdown();

        assertEquals(data, concurrencyGroup.getOutputQueue().take());
    }

    /**
     * Tests awaiting the termination of the threads of the concurrency-group.
     */
    @Test
    @DisplayName("Tests awaiting termination.")
    void testAwaitTermination() {
        ConcurrencyGroup concurrencyGroup = new ConcurrencyGroup(List.of(), new LinkedBlockingQueue<>(), "group-id", null);
        assertFalse(concurrencyGroup.awaitTermination());

        JobExecution jobExecution = new JobExecution();
        jobExecution.setExecutionState(JobExecutionState.RUNNING);
        concurrencyGroup.setJobExecution(jobExecution);
        assertFalse(concurrencyGroup.awaitTermination());

        jobExecution.setExecutionState(JobExecutionState.FINISHED);
        assertTrue(concurrencyGroup.awaitTermination());
    }

    /**
     * Tests handling exceptions.
     */
    @Test
    @DisplayName("Tests exception handling.")
    void testExceptionHandling() {
        JobExecution jobExecution = new JobExecution();
        jobExecution.setExecutionState(JobExecutionState.CANCELLED);

        ConcurrencyGroup concurrencyGroup = new ConcurrencyGroup(List.of(), new LinkedBlockingQueue<>(), "group-id", jobExecution);

        concurrencyGroup.uncaughtException(null, new IgorException("test-exception"));

        assertEquals(JobExecutionState.CANCELLED, concurrencyGroup.getJobExecution().getExecutionState());

        jobExecution.setExecutionState(JobExecutionState.RUNNING);
        concurrencyGroup.uncaughtException(null, new IgorException("test-exception"));

        assertEquals(JobExecutionState.FAILED, concurrencyGroup.getJobExecution().getExecutionState());
    }

}
