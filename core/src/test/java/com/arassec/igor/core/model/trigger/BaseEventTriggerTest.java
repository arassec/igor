package com.arassec.igor.core.model.trigger;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link BaseEventTrigger}.
 */
@DisplayName("Base-Event-Trigger Tests.")
class BaseEventTriggerTest {

    /**
     * Tests the {@link BaseEventTrigger}'s constructor.
     */
    @Test
    @DisplayName("Tests the base-event-trigger's constructor.")
    void testBaseEventTrigger() {
        BaseEventTrigger baseEventTrigger = mock(BaseEventTrigger.class,
                withSettings().useConstructor("category-id", "type-id").defaultAnswer(CALLS_REAL_METHODS));
        assertEquals("category-id", baseEventTrigger.getCategoryId());
        assertEquals("type-id", baseEventTrigger.getTypeId());
    }

    /**
     * Tests setting the event queue.
     */
    @Test
    @DisplayName("Tests setting the event queue.")
    void testSetEventQueue() {
        BaseEventTrigger baseEventTrigger = mock(BaseEventTrigger.class,
                withSettings().useConstructor("category-id", "type-id").defaultAnswer(CALLS_REAL_METHODS));
        LinkedBlockingQueue<Map<String, Object>> queue = new LinkedBlockingQueue<>();
        baseEventTrigger.setEventQueue(queue);
        assertEquals(queue, ReflectionTestUtils.getField(baseEventTrigger, "eventQueue"));
    }

    /**
     * Tests getting simulation data.
     */
    @Test
    @DisplayName("Tests getting simulation data.")
    void testGetSimulationData() {
        BaseEventTrigger baseEventTrigger = mock(BaseEventTrigger.class,
                withSettings().useConstructor("category-id", "type-id").defaultAnswer(CALLS_REAL_METHODS));

        assertEquals(Map.of(), baseEventTrigger.getSimulationData());

        baseEventTrigger.setSimulationData("{a:\"b\",\nc:\"d\",\ne:42\n}");

        Map<String, Object> data = baseEventTrigger.getSimulationData();
        assertEquals(3, data.size());
        assertEquals("b", data.get("a"));
        assertEquals("d", data.get("c"));
        assertEquals(42, data.get("e"));
    }

}
