package com.arassec.igor.plugin.message.connector.rabbitmq.validation;

import com.arassec.igor.plugin.message.connector.rabbitmq.RabbitMqMessageConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ExchangeAndOrQueueSetValidator}.
 */
@DisplayName("RabbitMQ 'queue and/or exchange set'-validator tests.")
class ExchangeAndOrQueueSetValidatorTest {

    /**
     * Mock for the validated connector.
     */
    private RabbitMqMessageConnector connectorMock;

    /**
     * Mock for the validation context.
     */
    private ConstraintValidatorContext validatorContextMock;

    /**
     * The validator under test.
     */
    private final ExchangeAndOrQueueSetValidator validator = new ExchangeAndOrQueueSetValidator();

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        connectorMock = mock(RabbitMqMessageConnector.class);
        validatorContextMock = mock(ConstraintValidatorContext.class);
    }

    /**
     * Tests validation of a RabbitMQ connector's configuration without a configuration.
     */
    @Test
    @DisplayName("Tests validation of a RabbitMQ connector's configuration without a configuration.")
    void testIsValidSucceedNoConnector() {
        assertTrue(validator.isValid(null, null));
    }

    /**
     * Tests validation of an incomplete configuration.
     */
    @Test
    @DisplayName("Tests validation of an incomplete configuration.")
    void testIsValidFailMissingConfiguration() {
        ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilderMock =
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

        when(validatorContextMock.getDefaultConstraintMessageTemplate()).thenReturn("igor-test");
        when(validatorContextMock.buildConstraintViolationWithTemplate("igor-test")).thenReturn(constraintViolationBuilderMock);

        ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilderCustomizableContextMock =
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);
        when(constraintViolationBuilderMock.addPropertyNode(anyString())).thenReturn(nodeBuilderCustomizableContextMock);

        assertFalse(validator.isValid(connectorMock, validatorContextMock));

        verify(validatorContextMock, times(1)).disableDefaultConstraintViolation();
        verify(constraintViolationBuilderMock, times(1)).addPropertyNode("exchange");
        verify(nodeBuilderCustomizableContextMock, times(1)).addConstraintViolation();
    }

    /**
     * Tests validation succeed with configured exchange.
     */
    @Test
    @DisplayName("Tests validation succeed with configured exchange.")
    void testIsValidSucceedExchangeConfigured() {
        when(connectorMock.getExchange()).thenReturn("test-exchange");
        assertTrue(validator.isValid(connectorMock, validatorContextMock));
    }

    /**
     * Tests validation succeed with configured queue.
     */
    @Test
    @DisplayName("Tests validation succeed with configured queue.")
    void testIsValidSucceedQueueConfigured() {
        when(connectorMock.getQueue()).thenReturn("test-queue");
        assertTrue(validator.isValid(connectorMock, validatorContextMock));
    }

}
