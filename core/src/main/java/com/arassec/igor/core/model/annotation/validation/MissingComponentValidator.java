package com.arassec.igor.core.model.annotation.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator that always fails. Used on igor components that indicate a missing component after it has been removed from the
 * classpath.
 */
public class MissingComponentValidator implements ConstraintValidator<MissingComponent, Object> {

    /**
     * Always returns false since the validator is only used on invalid components to indicate a dependency problem to the user.
     *
     * @param object                     The object to validate.
     * @param constraintValidatorContext The context.
     *
     * @return Always {@code false}.
     */
    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        return false;
    }

}
