package com.arassec.igor.module.misc.util.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste the {@link ValidCronExpressionValidator}.
 */
@DisplayName("Tests CRON expression validation.")
public class ValidCronExpressionValidatorTest {

    /**
     * Tests validation.
     */
    @Test
    @DisplayName("Tests validation.")
    void testIsValid() {
        ValidCronExpressionValidator validator = new ValidCronExpressionValidator();

        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
        assertTrue(validator.isValid("0 0 * * * *", null));
        assertTrue(validator.isValid("*/10 * * * * *", null));
        assertTrue(validator.isValid("0 */15 * * * *", null));
        assertTrue(validator.isValid("0 0 8,10 * * *", null));
        assertTrue(validator.isValid("0 0/30 8-10 * * *", null));
        assertTrue(validator.isValid("0 0 9-17 * * MON-FRI", null));
        assertTrue(validator.isValid("0 0 0 25 12 ?", null));

        assertFalse(validator.isValid("0 0 9-17", null));
        assertFalse(validator.isValid("INVALID", null));
    }

}
