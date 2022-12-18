package com.arassec.igor.plugin.core.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates a string as JSON object.
 */
public class ValidJsonObjectValidator implements ConstraintValidator<ValidJsonObject, String> {

    /**
     * Jackson's {@link ObjectMapper}.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Uses {@link ObjectMapper#readTree(String)} to validate the supplied string.
     *
     * @param input                      The input to validate.
     * @param constraintValidatorContext The validator context.
     *
     * @return {@code true} if the string can be used as JSON object, {@code false} otherwise.
     */
    @Override
    public boolean isValid(String input, ConstraintValidatorContext constraintValidatorContext) {
        if (input == null || input.isBlank()) {
            return true;
        }
        try {
            objectMapper.readTree(input);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

}
