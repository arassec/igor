package com.arassec.igor.module.web.trigger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Queue;

import static org.mockito.Mockito.*;

/**
 * Tests the {@link WebHookTriggerTest}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("'Web-Hook' trigger tests.")
class WebHookTriggerTest {

    /**
     * Tests processing an event.
     */
    @Test
    @DisplayName("Tests processing an event.")
    void testProcessEvent() {
        @SuppressWarnings("unchecked")
        Queue<Map<String, Object>> queueMock = mock(Queue.class);

        WebHookTrigger webHookTrigger = new WebHookTrigger();
        webHookTrigger.setEventQueue(queueMock);

        Map<String, Object> data = Map.of();
        webHookTrigger.processEvent(null);
        verify(queueMock, times(1)).add(eq(data));

        data = Map.of("key", "value");
        webHookTrigger.processEvent(data);
        verify(queueMock, times(1)).add(eq(data));
    }

}
