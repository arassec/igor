package com.arassec.igor.module.misc.trigger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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

    /**
     * Tests the validation of the CRON expression.
     */
    @Test
    @DisplayName("Tests RegExp validation.")
    void testValidation() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        CronTrigger cronTrigger = new CronTrigger();
        cronTrigger.setCronExpression("0 0 * * * *");

        Set<ConstraintViolation<CronTrigger>> violations = validator.validate(cronTrigger);
        assertTrue(violations.isEmpty());

        cronTrigger.setCronExpression("*/10 * * * * *");
        violations = validator.validate(cronTrigger);
        assertTrue(violations.isEmpty());

        cronTrigger.setCronExpression("0 */15 * * * *");
        violations = validator.validate(cronTrigger);
        assertTrue(violations.isEmpty());

        cronTrigger.setCronExpression("0 0 8,10 * * *");
        violations = validator.validate(cronTrigger);
        assertTrue(violations.isEmpty());

        cronTrigger.setCronExpression("0 0/30 8-10 * * *");
        violations = validator.validate(cronTrigger);
        assertTrue(violations.isEmpty());

        cronTrigger.setCronExpression("0 0 9-17 * * MON-FRI");
        violations = validator.validate(cronTrigger);
        assertTrue(violations.isEmpty());

        cronTrigger.setCronExpression("0 0 9-17 * * MON-FRI");
        violations = validator.validate(cronTrigger);
        assertTrue(violations.isEmpty());

        cronTrigger.setCronExpression("0 0 0 25 12 ?");
        violations = validator.validate(cronTrigger);
        assertTrue(violations.isEmpty());

        cronTrigger.setCronExpression("INVALID");
        violations = validator.validate(cronTrigger);
        assertFalse(violations.isEmpty());
    }

}