package com.arassec.igor.module.message.connector.rabbitmq;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.message.connector.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.SocketUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link RabbitMqMessageConnector}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests the RabbitMQ-Message-Connector.")
class RabbitMqMessageConnectorTest {

    /**
     * Tests initializing the component.
     */
    @Test
    @DisplayName("Tests initializing the connector.")
    void testInitialize() {
        RabbitMqMessageConnector rabbitMqMessageConnector = new RabbitMqMessageConnector();
        assertNull(rabbitMqMessageConnector.getRabbitTemplate());

        rabbitMqMessageConnector.initialize("job-id", new JobExecution());
        assertNotNull(rabbitMqMessageConnector.getRabbitTemplate());
    }

    /**
     * Tests sending a message.
     */
    @Test
    @DisplayName("Tests sending a message.")
    void testSendMessage() {
        RabbitMqMessageConnector rabbitMqMessageConnector = new RabbitMqMessageConnector();
        rabbitMqMessageConnector.setExchange("test-exchange");
        rabbitMqMessageConnector.setRoutingKey("test-routing-key");
        rabbitMqMessageConnector.setContentEncoding(StandardCharsets.UTF_8.displayName());
        rabbitMqMessageConnector.setContentType(MimeTypeUtils.APPLICATION_JSON_VALUE);
        rabbitMqMessageConnector.setHeaders("a:b\nc:d");

        RabbitTemplate rabbitTemplateMock = mock(RabbitTemplate.class);
        rabbitMqMessageConnector.setRabbitTemplate(rabbitTemplateMock);

        Message message = new Message();
        message.setContent("test-message");

        rabbitMqMessageConnector.sendMessage(message);

        ArgumentCaptor<org.springframework.amqp.core.Message> argCap =
                ArgumentCaptor.forClass(org.springframework.amqp.core.Message.class);

        verify(rabbitTemplateMock, times(1)).send(eq("test-exchange"), eq("test-routing-key"), argCap.capture());

        assertEquals("test-message", new String(argCap.getValue().getBody()));
        MessageProperties messageProperties = argCap.getValue().getMessageProperties();
        assertEquals("b", messageProperties.getHeader("a"));
        assertEquals("d", messageProperties.getHeader("c"));
        assertEquals(StandardCharsets.UTF_8.displayName(), messageProperties.getContentEncoding());
        assertEquals(MimeTypeUtils.APPLICATION_JSON_VALUE, messageProperties.getContentType());
    }

    /**
     * Tests shutting the connector down.
     */
    @Test
    @DisplayName("Tests shutting the connector down.")
    void testShutdown() {
        RabbitTemplate rabbitTemplateSpy = spy(new RabbitTemplate());
        CachingConnectionFactory connectionFactorySpy = spy(new CachingConnectionFactory());

        RabbitMqMessageConnector rabbitMqMessageConnector = new RabbitMqMessageConnector();
        rabbitMqMessageConnector.setRabbitTemplate(rabbitTemplateSpy);
        rabbitMqMessageConnector.setConnectionFactory(connectionFactorySpy);

        rabbitMqMessageConnector.shutdown("job-id", new JobExecution());

        verify(rabbitTemplateSpy, times(1)).stop();
        verify(connectionFactorySpy, times(1)).destroy();
    }

    /**
     * Tests testing a configuration.
     */
    @Test
    @DisplayName("Tests testing a RabbitMQ configuration")
    void testTestConfiguration() {
        RabbitMqMessageConnector rabbitMqMessageConnector = new RabbitMqMessageConnector();
        rabbitMqMessageConnector.setHost("localhost");
        rabbitMqMessageConnector.setPort(SocketUtils.findAvailableTcpPort());
        rabbitMqMessageConnector.setUsername("igor-test");
        rabbitMqMessageConnector.setPassword("invalid");
        rabbitMqMessageConnector.setConnectionTimeout(1);

        assertThrows(AmqpIOException.class, rabbitMqMessageConnector::testConfiguration);
    }

}
