package com.arassec.igor.core.util.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Bean validation constraint that checks CRON expression compatability with Spring.
 */
@Target({ElementType.FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidCronExpressionValidator.class)
@Documented
public @interface ValidCronExpression {

    /**
     * Returns the message key.
     *
     * @return The message key.
     */
    @SuppressWarnings("unused")
    String message() default "{com.arassec.igor.misc.validation.cron-expression}";

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
