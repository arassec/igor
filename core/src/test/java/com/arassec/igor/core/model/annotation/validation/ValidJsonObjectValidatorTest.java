package com.arassec.igor.core.model.annotation.validation;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste the {@link ValidJsonObjectValidator}.
 */
@DisplayName("Tests JSON object validation.")
class ValidJsonObjectValidatorTest {

    /**
     * Tests validation.
     */
    @Test
    @DisplayName("Tests validation.")
    void testIsValid() {
        ValidJsonObjectValidator validJsonObjectValidator = new ValidJsonObjectValidator();

        assertFalse(validJsonObjectValidator.isValid("invalid-json", null));

        assertTrue(validJsonObjectValidator.isValid(null, null));
        assertTrue(validJsonObjectValidator.isValid("", null));

        assertTrue(validJsonObjectValidator.isValid("{\"a\": 123, \"b\": \"c\"}", null));
    }

}
