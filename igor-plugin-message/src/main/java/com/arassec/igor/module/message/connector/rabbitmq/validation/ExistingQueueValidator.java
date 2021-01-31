package com.arassec.igor.module.message.connector.rabbitmq.validation;

import com.arassec.igor.module.message.connector.rabbitmq.RabbitMqMessageConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Validator that makes sure a configured queue exists on the RabbitMQ server and can be accessed.
 */
@Slf4j
public class ExistingQueueValidator implements ConstraintValidator<ExistingQueue, RabbitMqMessageConnector> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid(RabbitMqMessageConnector rabbitMqMessageConnector, ConstraintValidatorContext constraintValidatorContext) {
        if (rabbitMqMessageConnector == null) {
            return true;
        }

        if (!StringUtils.hasText(rabbitMqMessageConnector.getHost())
                || !StringUtils.hasText(rabbitMqMessageConnector.getUsername())
                || !StringUtils.hasText(rabbitMqMessageConnector.getPassword())
                || !StringUtils.hasText(rabbitMqMessageConnector.getVirtualHost())
                || !StringUtils.hasText(rabbitMqMessageConnector.getQueue())
        ) {
            return true;
        }

        CachingConnectionFactory testConnectionFactory =
                new CachingConnectionFactory(rabbitMqMessageConnector.getHost(), rabbitMqMessageConnector.getPort());
        testConnectionFactory.setUsername(rabbitMqMessageConnector.getUsername());
        testConnectionFactory.setPassword(rabbitMqMessageConnector.getPassword());
        testConnectionFactory.setVirtualHost(rabbitMqMessageConnector.getVirtualHost());

        RabbitAdmin rabbitAdmin = new RabbitAdmin(testConnectionFactory);

        boolean result = false;
        try {
            result = Optional.ofNullable(rabbitAdmin.getQueueInfo(rabbitMqMessageConnector.getQueue())).isPresent();
        } catch (Exception e) {
            log.debug("Exception during RabbitMQ connector validation: {}", e.getMessage());
        }

        if (!result) { //NOSONAR - seems to be a bug in 'java:S2583'
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    constraintValidatorContext.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("queue")
                    .addConstraintViolation();
        }

        return result;
    }

}
