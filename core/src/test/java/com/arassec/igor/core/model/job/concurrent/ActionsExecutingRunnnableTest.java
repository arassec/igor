package com.arassec.igor.core.model.job.concurrent;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ActionsExecutingRunnable}.
 */
@DisplayName("Tests the runnable that is responsible for actions")
class ActionsExecutingRunnnableTest {

    /**
     * Tests input validation on instance creation.
     */
    @Test
    @DisplayName("Tests input validation.")
    void testInputvalidation() {
        assertThrows(IllegalArgumentException.class, () -> new ActionsExecutingRunnable(null, null, null, null));
        LinkedBlockingQueue<Map<String, Object>> linkedBlockingQueue = new LinkedBlockingQueue<>();
        assertThrows(IllegalArgumentException.class, () -> new ActionsExecutingRunnable(null, null, linkedBlockingQueue, null));
        assertThrows(IllegalArgumentException.class, () -> new ActionsExecutingRunnable(null, linkedBlockingQueue, null, null));
    }

    /**
     * Tests actually running the runnable.
     */
    @Test
    @DisplayName("Tests actually running the runnable.")
    void testRun() {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put(DataKey.META.getKey(), Job.createMetaData("job-id", null, 25));

        BlockingQueue<Map<String, Object>> inputQueue = new LinkedBlockingQueue<>();
        inputQueue.offer(inputData);

        Map<String, Object> outputData = new HashMap<>();
        outputData.put("foo", "bar");

        BlockingQueue<Map<String, Object>> outputQueue = new LinkedBlockingQueue<>();

        List<Action> actions = new LinkedList<>();
        Action firstActionMock = mock(Action.class);
        actions.add(firstActionMock);
        Action secondActionMock = mock(Action.class);
        actions.add(secondActionMock);

        ActionsExecutingRunnable actionsExecutingRunnable = new ActionsExecutingRunnable(actions,
                inputQueue, outputQueue, null);

        when(firstActionMock.process(eq(inputData), nullable(JobExecution.class))).thenAnswer(invocationOnMock -> {
            // Simulates the job saying: 'finish your work, but no new items will be published'
            actionsExecutingRunnable.shutdown();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = List.of((Map<String, Object>) invocationOnMock.getArgument(0));
            return result;
        });

        when(secondActionMock.process(eq(inputData), nullable(JobExecution.class))).thenAnswer(invocationOnMock -> List.of(outputData));

        // Finally run the runnable:
        actionsExecutingRunnable.run();

        // The last action's output should be in the output queue:
        assertEquals(outputData, outputQueue.poll());
    }

    /**
     * Tests that final data items are processed on completion.
     */
    @Test
    @DisplayName("Tests completing the runnable.")
    void testComplete() {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put(DataKey.META.getKey(), Job.createMetaData("job-id", null, 25));

        BlockingQueue<Map<String, Object>> inputQueue = new LinkedBlockingQueue<>();
        BlockingQueue<Map<String, Object>> outputQueue = new LinkedBlockingQueue<>();

        Map<String, Object> outputData = new HashMap<>();
        outputData.put("foo", "bar");

        List<Action> actions = new LinkedList<>();
        Action firstActionMock = mock(Action.class);
        actions.add(firstActionMock);
        Action secondActionMock = mock(Action.class);
        actions.add(secondActionMock);

        ActionsExecutingRunnable actionsExecutingRunnable = new ActionsExecutingRunnable(actions,
                inputQueue, outputQueue, null);

        when(firstActionMock.complete()).thenReturn(List.of(inputData));

        when(secondActionMock.process(eq(inputData), nullable(JobExecution.class))).thenReturn(List.of(outputData));

        actionsExecutingRunnable.complete();

        assertEquals(outputData, outputQueue.poll());
    }

}
