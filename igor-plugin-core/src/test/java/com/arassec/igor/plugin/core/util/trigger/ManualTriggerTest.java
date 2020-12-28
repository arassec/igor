package com.arassec.igor.plugin.core.util.trigger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link ManualTrigger}.
 */
@DisplayName("'Manual trigger' tests.")
class ManualTriggerTest {

    /**
     * Tests the trigger.
     */
    @Test
    @DisplayName("Tests the trigger.")
    void testTrigger() {
        ManualTrigger trigger = new ManualTrigger();
        assertEquals("manual-trigger", trigger.getTypeId());
    }

}