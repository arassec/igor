package com.arassec.igor.module.message.connector.rabbitmq.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Bean validation constraint that checks that at least one of exchange or queue are configured.
 */
@Target({ElementType.TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = ExchangeAndOrQueueSetValidator.class)
@Documented
public @interface ExchangeAndOrQueueSet {

    /**
     * The message that indicates a failed validation.
     */
    String MESSAGE_KEY = "com.arassec.igor.message.validation.exchange-and-or-queue-set";

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
