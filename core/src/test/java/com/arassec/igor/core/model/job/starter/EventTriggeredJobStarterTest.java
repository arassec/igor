package com.arassec.igor.core.model.job.starter;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.concurrent.ConcurrencyGroup;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.trigger.EventTrigger;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link EventTriggeredJobStarter}.
 */
@Slf4j
@DisplayName("Event-Triggered Job-Starter tests.")
class EventTriggeredJobStarterTest {

    /**
     * Tests processing the job.
     */
    @Test
    @SneakyThrows
    @DisplayName("Tests processing the job.")
    void testProcess() {
        Action action = mock(Action.class);
        when(action.isActive()).thenReturn(true);

        EventTrigger eventTrigger = mock(EventTrigger.class);

        JobExecution jobExecution = spy(JobExecution.class);
        jobExecution.setExecutionState(JobExecutionState.ACTIVE);

        // Save the blocking queue created in the starter:
        AtomicReference<BlockingQueue<Map<String, Object>>> triggerQueueReference = new AtomicReference<>();
        doAnswer(invocationOnMock -> {
            triggerQueueReference.set(invocationOnMock.getArgument(0));
            log.debug("Trigger input-queue has been set.");
            return null;
        }).when(eventTrigger).setEventQueue(any());

        // Add a data item after initialization to proceed with the processing:
        doAnswer(invocationOnMock -> {
            log.debug("Putting dummy data item to trigger input-queue.");
            triggerQueueReference.get().put(new HashMap<>());
            return null;
        }).when(eventTrigger).initialize(jobExecution);

        // Stop the process after the data item has been processed:
        AtomicBoolean eventProcessed = new AtomicBoolean(false);
        doAnswer(invocationOnMock -> {
            log.debug("Dummy data item has been processed. Setting job state to {}", JobExecutionState.CANCELLED);
            eventProcessed.set(true);
            jobExecution.setExecutionState(JobExecutionState.CANCELLED);
            return null;
        }).when(jobExecution).setProcessedEvents(1);

        EventTriggeredJobStarter eventTriggeredJobStarter = new EventTriggeredJobStarter(eventTrigger, List.of(action),
            jobExecution, 1);

        List<ConcurrencyGroup> concurrencyGroups = eventTriggeredJobStarter.process();

        assertNotNull(concurrencyGroups);
        assertEquals(1, concurrencyGroups.size());

        verify(action, times(1)).initialize(jobExecution);
        assertTrue(eventProcessed.get());
    }

}
