package com.arassec.igor.module.message.service.rabbitmq;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.message.service.Message;
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
 * Tests the {@link RabbitMqMessageService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests the RabbitMQ-Message-Service.")
public class RabbitMqMessageServiceTest {

    /**
     * Tests initializing the component.
     */
    @Test
    @DisplayName("Tests initializing the service.")
    void testInitialize() {
        RabbitMqMessageService rabbitMqMessageService = new RabbitMqMessageService();
        assertNull(rabbitMqMessageService.getRabbitTemplate());

        rabbitMqMessageService.initialize("job-id", "task-id", new JobExecution());
        assertNotNull(rabbitMqMessageService.getRabbitTemplate());
    }

    /**
     * Tests sending a message.
     */
    @Test
    @DisplayName("Tests sending a message.")
    void testSendMessage() {
        RabbitMqMessageService rabbitMqMessageService = new RabbitMqMessageService();
        rabbitMqMessageService.setExchange("test-exchange");
        rabbitMqMessageService.setRoutingKey("test-routing-key");
        rabbitMqMessageService.setContentEncoding(StandardCharsets.UTF_8.displayName());
        rabbitMqMessageService.setContentType(MimeTypeUtils.APPLICATION_JSON_VALUE);
        rabbitMqMessageService.setHeaders("a:b\nc:d");

        RabbitTemplate rabbitTemplateMock = mock(RabbitTemplate.class);
        rabbitMqMessageService.setRabbitTemplate(rabbitTemplateMock);

        Message message = new Message();
        message.setContent("test-message");

        rabbitMqMessageService.sendMessage(message);

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
     * Tests shutting the service down.
     */
    @Test
    @DisplayName("Tests shutting the service down.")
    void testShutdown() {
        RabbitTemplate rabbitTemplateSpy = spy(new RabbitTemplate());
        CachingConnectionFactory connectionFactorySpy = spy(new CachingConnectionFactory());

        RabbitMqMessageService rabbitMqMessageService = new RabbitMqMessageService();
        rabbitMqMessageService.setRabbitTemplate(rabbitTemplateSpy);
        rabbitMqMessageService.setConnectionFactory(connectionFactorySpy);

        rabbitMqMessageService.shutdown("job-id", "task-id", new JobExecution());

        verify(rabbitTemplateSpy, times(1)).stop();
        verify(connectionFactorySpy, times(1)).destroy();
    }

    /**
     * Tests testing a configuration.
     */
    @Test
    @DisplayName("Tests testing a RabbitMQ configuration")
    void testTestConfiguration() {
        RabbitMqMessageService rabbitMqMessageService = new RabbitMqMessageService();
        rabbitMqMessageService.setHost("localhost");
        rabbitMqMessageService.setPort(SocketUtils.findAvailableTcpPort());
        rabbitMqMessageService.setUsername("igor-test");
        rabbitMqMessageService.setPassword("invalid");
        rabbitMqMessageService.setConnectionTimeout(1);

        assertThrows(AmqpIOException.class, rabbitMqMessageService::testConfiguration);
    }

}
