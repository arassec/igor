package com.arassec.igor.core.util.validation;

import org.springframework.scheduling.support.CronTrigger;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates a CRON expression against Spring's scheduler requirements.
 */
public class ValidCronExpressionValidator implements ConstraintValidator<ValidCronExpression, String> {

    /**
     * Uses Spring's {@link CronTrigger} to validate the supplied CRON expression.
     *
     * @param cronExpression             The expression to validate.
     * @param constraintValidatorContext The validator context.
     *
     * @return {@code true} if the CRON expression can be used with Spring's scheduling system, {@code false} otherwise.
     */
    @Override
    public boolean isValid(String cronExpression, ConstraintValidatorContext constraintValidatorContext) {
        if (cronExpression == null || cronExpression.isBlank()) {
            return true;
        }

        try {
            new CronTrigger(cronExpression);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return true;
    }
}
