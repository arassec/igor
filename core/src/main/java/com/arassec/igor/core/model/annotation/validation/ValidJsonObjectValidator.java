package com.arassec.igor.core.model.annotation.validation;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates a string as JSON object.
 */
public class ValidJsonObjectValidator implements ConstraintValidator<ValidJsonObject, String> {

    /**
     * Uses {@link net.minidev.json.JSONObject} to validate the supplied string.
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
        Object parsed = JSONValue.parse(input);
        return parsed instanceof JSONObject;
    }

}
