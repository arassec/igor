package com.arassec.igor.core.util.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Bean validation constraint that checks JSON objects.
 */
@Target({ElementType.FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidJsonObjectValidator.class)
@Documented
public @interface ValidJsonObject {

    /**
     * Returns the message key.
     *
     * @return The message key.
     */
    @SuppressWarnings("unused")
    String message() default "{com.arassec.igor.validation.json-object}";

    /**
     * Possible bean validation groups.
     *
     * @return The groups this validation is put in.
     */
    @SuppressWarnings("unused")
    Class<?>[] groups() default { };

    /**
     * Can be used to assign custom payload objects to a constraint.
     *
     * @return The payload.
     */
    @SuppressWarnings("unused")
    Class<? extends Payload>[] payload() default { };

}
