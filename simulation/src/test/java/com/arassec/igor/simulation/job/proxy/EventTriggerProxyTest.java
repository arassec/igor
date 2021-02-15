package com.arassec.igor.simulation.job.proxy;

import com.arassec.igor.core.model.trigger.EventTrigger;
import com.arassec.igor.core.model.trigger.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link EventTriggerProxy}.
 */
@DisplayName("Tests the Event-Trigger-Proxy.")
class EventTriggerProxyTest {

    /**
     * The proxy to test.
     */
    private EventTriggerProxy eventTriggerProxy;

    /**
     * The delegate.
     */
    private EventTrigger delegateMock;

    /**
     * Initializes the test classes.
     */
    @BeforeEach
    void initialize() {
        delegateMock = mock(EventTrigger.class);
        eventTriggerProxy = new EventTriggerProxy(delegateMock, 1);
    }

    /**
     * Tests setting the event queue for incoming events.
     */
    @Test
    @DisplayName("Tests setting the event queue for incoming events.")
    void testSetEventQueue() {
        BlockingDeque<Map<String, Object>> eventQueue = new LinkedBlockingDeque<>();
        eventTriggerProxy.setEventQueue(eventQueue);
        verify(delegateMock, times(1)).setEventQueue(eventQueue);
    }

    /**
     * Tests processing incoming events.
     */
    @Test
    @DisplayName("Tests processing incoming events.")
    void testProcessEvent() {
        Map<String, Object> data = new HashMap<>();

        eventTriggerProxy.processEvent(data);

        assertEquals(1, eventTriggerProxy.getProcessed());
        assertEquals(1, eventTriggerProxy.getCollectedData().size());
        verify(delegateMock, times(1)).processEvent(data);

        // Simulation limit is set to 1, so this call must be blocked:
        eventTriggerProxy.processEvent(data);

        assertEquals(2, eventTriggerProxy.getProcessed());
        assertEquals(1, eventTriggerProxy.getCollectedData().size());
        verify(delegateMock, times(1)).processEvent(data);
    }

    /**
     * Tests processing of processed data items.
     */
    @Test
    @DisplayName("Tests processing of processed data items.")
    void testProcessingFinished() {
        Map<String, Object> data = new HashMap<>();
        eventTriggerProxy.processingFinished(data);
        verify(delegateMock, times(1)).processingFinished(data);
    }

    /**
     * Tests retrieving the supported event type.
     */
    @Test
    @DisplayName("Tests retrieving the supported event type.")
    void testGetSupportedEventType() {
        when(delegateMock.getSupportedEventType()).thenReturn(EventType.MESSAGE);
        assertEquals(EventType.MESSAGE, eventTriggerProxy.getSupportedEventType());
    }

}
