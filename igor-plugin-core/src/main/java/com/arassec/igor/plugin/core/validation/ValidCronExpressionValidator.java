package com.arassec.igor.plugin.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.scheduling.support.CronTrigger;

/**
 * Validates a CRON expression against Spring's scheduler requirements.
 */
public class ValidCronExpressionValidator implements ConstraintValidator<ValidCronExpression, String> {

    /**
     * Validates the supplied CRON expression.
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
