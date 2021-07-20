package com.arassec.igor.core.model.trigger;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link BaseEventTrigger}.
 */
@DisplayName("Base-Event-Trigger Tests.")
class BaseEventTriggerTest {

    /**
     * The base class under test.
     */
    private final BaseEventTrigger baseEventTrigger = mock(BaseEventTrigger.class,
        withSettings().defaultAnswer(CALLS_REAL_METHODS));

    /**
     * Tests setting the event queue.
     */
    @Test
    @DisplayName("Tests setting the event queue.")
    void testSetEventQueue() {
        LinkedBlockingQueue<Map<String, Object>> queue = new LinkedBlockingQueue<>();
        baseEventTrigger.setEventQueue(queue);
        assertEquals(queue, ReflectionTestUtils.getField(baseEventTrigger, "eventQueue"));
    }

    /**
     * Tests processing an event.
     */
    @Test
    @DisplayName("Tests processing an event.")
    void testProcessEvent() {
        LinkedBlockingQueue<Map<String, Object>> queue = new LinkedBlockingQueue<>();
        baseEventTrigger.setEventQueue(queue);

        baseEventTrigger.processEvent(null);

        Map<String, Object> eventData = queue.poll();
        assertNotNull(eventData);
        assertTrue(eventData.isEmpty());

        baseEventTrigger.processEvent(Map.of("A", "B"));

        eventData = queue.poll();
        assertNotNull(eventData);
        assertEquals("B", eventData.get("A"));
    }

    /**
     * Tests getting the default value for supported event types.
     */
    @Test
    @DisplayName("Tests getting the default value for supported event type.")
    void testGetSupportedEventType() {
        assertEquals(EventType.NONE, baseEventTrigger.getSupportedEventType());
    }

}
