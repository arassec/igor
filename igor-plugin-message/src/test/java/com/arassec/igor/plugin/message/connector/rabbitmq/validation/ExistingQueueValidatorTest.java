package com.arassec.igor.plugin.message.connector.rabbitmq.validation;

import com.arassec.igor.plugin.message.connector.rabbitmq.RabbitMqMessageConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ExistingQueueValidator}.
 */
@DisplayName("RabbitMQ 'existing queue'-validator tests.")
class ExistingQueueValidatorTest {

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
    private final ExistingQueueValidator validator = new ExistingQueueValidator();

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        connectorMock = mock(RabbitMqMessageConnector.class);
        validatorContextMock = mock(ConstraintValidatorContext.class);
    }

    /**
     * Tests validation succeeds if no connector is provided.
     */
    @Test
    @DisplayName("Tests validation succeeds if no connector is provided.")
    void testIsValidSucceedNoConnector() {
        assertTrue(validator.isValid(null, null));
    }

    /**
     * Tests validation succeeds if not enough configuration is provided.
     */
    @Test
    @DisplayName("Tests validation succeeds if not enough configuration is provided.")
    void testIsValidSucceedNoConfiguration() {
        assertTrue(validator.isValid(connectorMock, validatorContextMock));

        when(connectorMock.getHost()).thenReturn("host");
        assertTrue(validator.isValid(connectorMock, validatorContextMock));

        when(connectorMock.getUsername()).thenReturn("username");
        assertTrue(validator.isValid(connectorMock, validatorContextMock));

        when(connectorMock.getPassword()).thenReturn("password");
        assertTrue(validator.isValid(connectorMock, validatorContextMock));

        when(connectorMock.getVirtualHost()).thenReturn("virtualHost");
        assertTrue(validator.isValid(connectorMock, validatorContextMock));
    }

    /**
     * Tests validation fails if the queue is not accessible.
     */
    @Test
    @DisplayName("Tests validation fails if the queue is not accessible.")
    void testIsValidFailQueueNotAccessible() {
        when(connectorMock.getHost()).thenReturn("host");
        when(connectorMock.getUsername()).thenReturn("username");
        when(connectorMock.getPassword()).thenReturn("password");
        when(connectorMock.getVirtualHost()).thenReturn("virtualHost");
        when(connectorMock.getQueue()).thenReturn("queue");

        ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilderMock =
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

        when(validatorContextMock.getDefaultConstraintMessageTemplate()).thenReturn("igor-test");
        when(validatorContextMock.buildConstraintViolationWithTemplate("igor-test")).thenReturn(constraintViolationBuilderMock);

        ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilderCustomizableContextMock =
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);
        when(constraintViolationBuilderMock.addPropertyNode(anyString())).thenReturn(nodeBuilderCustomizableContextMock);

        assertFalse(validator.isValid(connectorMock, validatorContextMock));

        verify(validatorContextMock, times(1)).disableDefaultConstraintViolation();
        verify(constraintViolationBuilderMock, times(1)).addPropertyNode("queue");
        verify(nodeBuilderCustomizableContextMock, times(1)).addConstraintViolation();
    }

}
