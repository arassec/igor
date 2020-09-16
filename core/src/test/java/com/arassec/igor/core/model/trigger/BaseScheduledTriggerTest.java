package com.arassec.igor.core.model.trigger;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link BaseScheduledTrigger}.
 */
@DisplayName("Base-Scheduled-Trigger Tests.")
class BaseScheduledTriggerTest {

    /**
     * Tests the {@link BaseTrigger}'s constructor.
     */
    @Test
    @DisplayName("Tests the base-scheduled-trigger's constructor.")
    void testBaseScheduledTrigger() {
        BaseScheduledTrigger baseScheduledTrigger = mock(BaseScheduledTrigger.class,
                withSettings().useConstructor("category-id", "type-id").defaultAnswer(CALLS_REAL_METHODS));
        assertEquals("category-id", baseScheduledTrigger.getCategoryId());
        assertEquals("type-id", baseScheduledTrigger.getTypeId());
    }

    /**
     * Tests getting the configured CRON expression.
     */
    @Test
    @DisplayName("Tests getting the configured CRON expression.")
    void testGetCronExpression() {
        BaseScheduledTrigger baseScheduledTrigger = mock(BaseScheduledTrigger.class,
                withSettings().useConstructor("category-id", "type-id").defaultAnswer(CALLS_REAL_METHODS));

        baseScheduledTrigger.setCronExpression("* * */15 * * *");
        assertEquals("* * */15 * * *", baseScheduledTrigger.getCronExpression());
    }

}
