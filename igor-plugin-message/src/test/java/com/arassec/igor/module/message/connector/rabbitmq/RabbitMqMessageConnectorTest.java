package com.arassec.igor.module.message.connector.rabbitmq;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.trigger.EventType;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.core.util.event.JobTriggerEvent;
import com.arassec.igor.plugin.core.message.connector.Message;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.SocketUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link RabbitMqMessageConnector}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests the RabbitMQ-Message-Connector.")
class RabbitMqMessageConnectorTest {

    /**
     * Mock of Spring's {@link ApplicationEventPublisher}.
     */
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * Tests initializing the component.
     */
    @Test
    @DisplayName("Tests initializing the connector.")
    void testInitialize() {
        RabbitMqMessageConnector rabbitMqMessageConnector = new RabbitMqMessageConnector(applicationEventPublisher);
        rabbitMqMessageConnector.setHost("host");
        rabbitMqMessageConnector.setUsername("username");
        rabbitMqMessageConnector.setPassword("password");

        assertNull(rabbitMqMessageConnector.getRabbitTemplate());

        rabbitMqMessageConnector.initialize(new JobExecution());

        RabbitTemplate rabbitTemplate = rabbitMqMessageConnector.getRabbitTemplate();
        assertNotNull(rabbitMqMessageConnector.getRabbitTemplate());

        rabbitMqMessageConnector.initialize(new JobExecution());
        assertEquals(rabbitTemplate, rabbitMqMessageConnector.getRabbitTemplate());

        rabbitMqMessageConnector.initialize(JobExecution.builder().jobId("job-id").build());
        assertEquals("job-id", rabbitMqMessageConnector.getJobId());
    }

    /**
     * Tests sending a message.
     */
    @Test
    @DisplayName("Tests sending a message.")
    void testSendMessage() {
        RabbitMqMessageConnector rabbitMqMessageConnector = new RabbitMqMessageConnector(applicationEventPublisher);

        Message emptyMessage = new Message();
        assertThrows(IgorException.class, () -> rabbitMqMessageConnector.sendMessage(emptyMessage));

        rabbitMqMessageConnector.setExchange("test-exchange");

        assertThrows(IgorException.class, () -> rabbitMqMessageConnector.sendMessage(null));
        assertThrows(IgorException.class, () -> rabbitMqMessageConnector.sendMessage(emptyMessage));

        rabbitMqMessageConnector.setRoutingKey("test-routing-key");

        RabbitTemplate rabbitTemplateMock = mock(RabbitTemplate.class);
        rabbitMqMessageConnector.setRabbitTemplate(rabbitTemplateMock);

        Message message = new Message();
        message.setContentEncoding(StandardCharsets.UTF_8.displayName());
        message.setContentType(MimeTypeUtils.APPLICATION_JSON_VALUE);
        message.getHeaders().put("a", "b");
        message.getHeaders().put("c", "d");
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
     * Tests enabling message retrieval.
     */
    @Test
    @DisplayName("Tests enabling message retrieval.")
    void testEnableMessageRetrieval() {
        RabbitMqMessageConnector rabbitMqMessageConnector = new RabbitMqMessageConnector(applicationEventPublisher);

        // missing queue:
        assertThrows(IgorException.class, rabbitMqMessageConnector::enableMessageRetrieval);
        rabbitMqMessageConnector.setQueue("test-queue");

        // missing host:
        assertThrows(IgorException.class, rabbitMqMessageConnector::enableMessageRetrieval);
        rabbitMqMessageConnector.setHost("host");

        // missing username:
        assertThrows(IgorException.class, rabbitMqMessageConnector::enableMessageRetrieval);
        rabbitMqMessageConnector.setUsername("username");

        // missing password:
        assertThrows(IgorException.class, rabbitMqMessageConnector::enableMessageRetrieval);
        rabbitMqMessageConnector.setPassword("password");

        assertNull(rabbitMqMessageConnector.getConnectionFactory());
        assertNull(rabbitMqMessageConnector.getMessageListenerContainer());

        rabbitMqMessageConnector.enableMessageRetrieval();

        assertNotNull(rabbitMqMessageConnector.getConnectionFactory());
        assertNotNull(rabbitMqMessageConnector.getMessageListenerContainer());
    }

    /**
     * Tests shutting the connector down.
     */
    @Test
    @DisplayName("Tests shutting the connector down.")
    void testShutdown() {
        RabbitTemplate rabbitTemplateSpy = spy(new RabbitTemplate());
        CachingConnectionFactory connectionFactorySpy = spy(new CachingConnectionFactory());

        RabbitMqMessageConnector rabbitMqMessageConnector = new RabbitMqMessageConnector(applicationEventPublisher);

        // should work without exception:
        rabbitMqMessageConnector.shutdown(new JobExecution());

        // should shutdown established connections:
        rabbitMqMessageConnector.setRabbitTemplate(rabbitTemplateSpy);
        rabbitMqMessageConnector.setConnectionFactory(connectionFactorySpy);

        rabbitMqMessageConnector.shutdown(new JobExecution());

        verify(rabbitTemplateSpy, times(1)).destroy();
        verify(connectionFactorySpy, times(1)).destroy();
    }

    /**
     * Tests testing a configuration.
     */
    @Test
    @DisplayName("Tests testing a RabbitMQ configuration")
    void testTestConfiguration() {
        RabbitMqMessageConnector rabbitMqMessageConnector = new RabbitMqMessageConnector(applicationEventPublisher);
        rabbitMqMessageConnector.setHost("localhost");
        rabbitMqMessageConnector.setPort(SocketUtils.findAvailableTcpPort());
        rabbitMqMessageConnector.setUsername("igor-test");
        rabbitMqMessageConnector.setPassword("invalid");
        rabbitMqMessageConnector.setConnectionTimeout(1);

        assertThrows(AmqpIOException.class, rabbitMqMessageConnector::testConfiguration);
    }

    /**
     * Tests message retrieval.
     */
    @Test
    @DisplayName("Tests message retrieval.")
    void testOnMessage() {
        org.springframework.amqp.core.Message messageMock = mock(org.springframework.amqp.core.Message.class);
        Channel channelMock = mock(Channel.class);

        RabbitMqMessageConnector rabbitMqMessageConnector = new RabbitMqMessageConnector(applicationEventPublisher);
        rabbitMqMessageConnector.setJobId("job-id");

        // missing message body:
        assertThrows(IgorException.class, () -> rabbitMqMessageConnector.onMessage(messageMock, channelMock));
        when(messageMock.getBody()).thenReturn("test-message".getBytes());

        // missing message properties:
        assertThrows(IgorException.class, () -> rabbitMqMessageConnector.onMessage(messageMock, channelMock));
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setDeliveryTag(123456L);
        when(messageMock.getMessageProperties()).thenReturn(messageProperties);

        rabbitMqMessageConnector.onMessage(messageMock, channelMock);

        ArgumentCaptor<JobTriggerEvent> argCap = ArgumentCaptor.forClass(JobTriggerEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(argCap.capture());

        JobTriggerEvent event = argCap.getValue();
        assertEquals("job-id", event.getJobId());
        assertEquals(EventType.MESSAGE, event.getEventType());
        assertEquals("{message=test-message, messageMeta={deliveryTag=123456}}", event.getEventData().toString());

        assertEquals(channelMock, rabbitMqMessageConnector.getChannels().get(123456L));
    }

    /**
     * Tests processing finished data items.
     */
    @Test
    @DisplayName("Tests processing finished data items.")
    @SneakyThrows
    void testProcessingFinished() {
        RabbitMqMessageConnector rabbitMqMessageConnector = new RabbitMqMessageConnector(applicationEventPublisher);

        // empty data item:
        final Map<String, Object> emptyDataItem = Map.of();
        assertThrows(IgorException.class, () -> rabbitMqMessageConnector.processingFinished(emptyDataItem));

        // channel doesn't exist:
        Map<String, Object> dataItem = Map.of("data", Map.of("messageMeta", Map.of("deliveryTag", 123L)));
        rabbitMqMessageConnector.processingFinished(dataItem);

        // channel exists:
        Channel channelMock = mock(Channel.class);
        rabbitMqMessageConnector.getChannels().put(123L, channelMock);

        rabbitMqMessageConnector.processingFinished(dataItem);

        assertFalse(rabbitMqMessageConnector.getChannels().containsKey(123L));
        verify(channelMock, times(1)).basicAck(123L, false);
    }

}
