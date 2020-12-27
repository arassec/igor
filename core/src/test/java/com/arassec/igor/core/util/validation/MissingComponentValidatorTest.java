package com.arassec.igor.core.util.validation;

import com.arassec.igor.core.model.action.MissingComponentAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link MissingComponentValidator}.
 */
@DisplayName("Missing-Component-Validator tests")
class MissingComponentValidatorTest {

    /**
     * Tests that vaildation with this validator always fails.
     */
    @Test
    @DisplayName("Tests that vaildation with this validator always fails")
    void testIsValid() {
        MissingComponentValidator validator = new MissingComponentValidator();
        assertFalse(validator.isValid(null, null));
        assertFalse(validator.isValid(new MissingComponentAction("unit-test"), mock(ConstraintValidatorContext.class)));
    }

}
