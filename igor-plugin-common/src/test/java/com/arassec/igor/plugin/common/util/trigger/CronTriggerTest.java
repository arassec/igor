package com.arassec.igor.plugin.common.util.trigger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link CronTrigger}.
 */
@DisplayName("'CRON trigger' tests.")
class CronTriggerTest {

    /**
     * Tests the CRON trigger.
     */
    @Test
    @DisplayName("Tests the CRON trigger.")
    void testTrigger() {
        CronTrigger cronTrigger = new CronTrigger();
        cronTrigger.setCronExpression("0 0 * * * *");
        assertEquals("0 0 * * * *", cronTrigger.getCronExpression());
    }

}