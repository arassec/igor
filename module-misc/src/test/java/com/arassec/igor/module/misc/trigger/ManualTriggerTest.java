package com.arassec.igor.module.misc.trigger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals("4af90cde-1da2-4d1e-a582-21443af3955b", trigger.getTypeId());
    }

}