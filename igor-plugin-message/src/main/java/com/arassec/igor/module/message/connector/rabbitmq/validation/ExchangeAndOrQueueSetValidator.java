package com.arassec.igor.module.message.connector.rabbitmq.validation;

import com.arassec.igor.module.message.connector.rabbitmq.RabbitMqMessageConnector;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates that at least one of exchange or queue are configured.
 * <p>
 * This could not be implemented as field validator because both properties, 'exchange' and 'queue', are required for validation.
 */
public class ExchangeAndOrQueueSetValidator implements ConstraintValidator<ExchangeAndOrQueueSet, RabbitMqMessageConnector> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid(RabbitMqMessageConnector rabbitMqMessageConnector, ConstraintValidatorContext constraintValidatorContext) {
        if (rabbitMqMessageConnector == null) {
            return true;
        }
        boolean result = StringUtils.hasText(rabbitMqMessageConnector.getExchange()) || StringUtils.hasText(rabbitMqMessageConnector.getQueue());
        if (!result) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    constraintValidatorContext.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("exchange")
                    .addConstraintViolation();
        }
        return result;
    }

}
