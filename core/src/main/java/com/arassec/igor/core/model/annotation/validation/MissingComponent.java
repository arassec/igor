package com.arassec.igor.core.model.annotation.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Bean validation constraint that always fails. Used to indicate a missing igor component to the user.
 */
@Target({ElementType.FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = MissingComponentValidator.class)
@Documented
public @interface MissingComponent {

    /**
     * The message that indicates a failed validation.
     */
    String MESSAGE_KEY = "{com.arassec.igor.validation.missing-component}";

    /**
     * Returns the message key.
     *
     * @return The message key.
     */
    String message() default MESSAGE_KEY;

    /**
     * Possible bean validation groups.
     *
     * @return The groups this validation is put in.
     */
    Class<?>[] groups() default {};

    /**
     * Can be used to assign custom payload objects to a constraint.
     *
     * @return The payload.
     */
    @SuppressWarnings("unused")
    Class<? extends Payload>[] payload() default {};

}
