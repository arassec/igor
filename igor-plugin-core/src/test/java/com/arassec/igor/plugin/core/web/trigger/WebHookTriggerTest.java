package com.arassec.igor.plugin.core.web.trigger;

import com.arassec.igor.core.model.trigger.EventType;
import com.arassec.igor.plugin.core.CoreCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link WebHookTriggerTest}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("'Web-Hook' trigger tests.")
class WebHookTriggerTest {

    /**
     * Tests initialization of the trigger.
     */
    @Test
    @DisplayName("Tests initialization of the trigger.")
    void testInitilization() {
        WebHookTrigger webHookTrigger = new WebHookTrigger();
        assertEquals(CoreCategory.WEB.getId(), webHookTrigger.getCategoryId());
        assertEquals("web-hook-trigger", webHookTrigger.getTypeId());
        assertEquals(EventType.WEB_HOOK, webHookTrigger.getSupportedEventType());
    }

}
