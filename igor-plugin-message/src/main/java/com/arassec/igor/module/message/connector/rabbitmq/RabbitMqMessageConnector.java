package com.arassec.igor.module.message.connector.rabbitmq;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.trigger.EventTrigger;
import com.arassec.igor.core.model.trigger.EventType;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.core.util.event.JobTriggerEvent;
import com.arassec.igor.module.message.connector.rabbitmq.validation.ExchangeAndOrQueueSet;
import com.arassec.igor.module.message.connector.rabbitmq.validation.ExistingQueue;
import com.arassec.igor.plugin.core.message.connector.BaseMessageConnector;
import com.arassec.igor.plugin.core.message.connector.Message;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.rabbitmq.client.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONValue;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Message connector to process messages via RabbitMQ.
 */
@Getter
@Setter
@Slf4j
@ExchangeAndOrQueueSet
@ExistingQueue
@IgorComponent
public class RabbitMqMessageConnector extends BaseMessageConnector implements ChannelAwareMessageListener {

    /**
     * Publisher for events based on job changes.
     */
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * The RabbitMQ host.
     */
    @NotBlank
    @IgorParam
    private String host;

    /**
     * The RabbitMQ port.
     */
    @Positive
    @IgorParam(defaultValue = "5672")
    private int port;

    /**
     * The RabbitMQ username.
     */
    @NotBlank
    @IgorParam
    private String username;

    /**
     * The RabbitMQ password.
     */
    @NotBlank
    @IgorParam(secured = true)
    private String password;

    /**
     * The exchange messages are sent to.
     */
    @IgorParam
    private String exchange;

    /**
     * The queue messages are received from.
     */
    @IgorParam
    private String queue;

    /**
     * The optional routing key for messages.
     */
    @IgorParam(advanced = true)
    private String routingKey;

    /**
     * The virtual host.
     */
    @NotBlank
    @IgorParam(advanced = true, defaultValue = "/")
    private String virtualHost;

    /**
     * Optional heartbeat for connections to the RabbitMQ server.
     */
    @Positive
    @IgorParam(advanced = true, defaultValue = "30")
    private int heartBeat;

    /**
     * Optional connection timeout.
     */
    @Positive
    @IgorParam(advanced = true, defaultValue = "60000")
    private int connectionTimeout;

    /**
     * Number of messages that are fetched at once without waiting for acknowledgements of the previous messages.
     */
    @Positive
    @IgorParam(advanced = true, defaultValue = "10")
    private int prefetchCount;

    /**
     * The job's ID.
     */
    private String jobId;

    /**
     * The connection factory to the RabbitMQ server.
     */
    private CachingConnectionFactory connectionFactory;

    /**
     * The RabbitMQ-Client.
     */
    private RabbitTemplate rabbitTemplate;

    /**
     * Container for receiving messages.
     */
    private DirectMessageListenerContainer messageListenerContainer;

    /**
     * Stores the RabbitMQ channels for acknowledging the messages.
     */
    private Map<Long, Channel> channels = Collections.synchronizedMap(new HashMap<>());

    /**
     * Creates a new component instance.
     *
     * @param applicationEventPublisher Spring's event publisher.
     */
    public RabbitMqMessageConnector(ApplicationEventPublisher applicationEventPublisher) {
        super("rabbitmq-message-connector");
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(JobExecution jobExecution) {
        super.initialize(jobExecution);
        this.jobId = jobExecution.getJobId();
        if (connectionFactory == null) {
            connectionFactory = createConnectionFactory();
        }
        if (rabbitTemplate == null) {
            rabbitTemplate = new RabbitTemplate(connectionFactory);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(Message message) {
        if (!StringUtils.hasText(exchange)) {
            throw new IgorException("No RabbitMQ exchange configured to send messages to!");
        }

        if (message == null || !StringUtils.hasText(message.getContent())) {
            throw new IgorException("Empty content provided for message sending!");
        }

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentEncoding(message.getContentEncoding());
        messageProperties.setContentType(message.getContentType());

        message.getHeaders().forEach((key, value) -> messageProperties.getHeaders().put(key, value));

        org.springframework.amqp.core.Message rabbitMessage = new org.springframework.amqp.core.Message(message.getContent().getBytes(), messageProperties);
        rabbitTemplate.send(exchange, routingKey, rabbitMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enableMessageRetrieval() {
        if (!StringUtils.hasText(queue)) {
            throw new IgorException("No RabbitMQ queue configured to listen for messages!");
        }

        if (connectionFactory == null) {
            connectionFactory = createConnectionFactory();
        }

        messageListenerContainer = new DirectMessageListenerContainer(connectionFactory);
        messageListenerContainer.setQueueNames(queue);
        messageListenerContainer.setMessageListener(new MessageListenerAdapter(this, "receiveMessage"));
        messageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        messageListenerContainer.setPrefetchCount(prefetchCount);
        messageListenerContainer.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown(JobExecution jobExecution) {
        super.shutdown(jobExecution);
        if (rabbitTemplate != null) {
            rabbitTemplate.destroy();
        }
        if (messageListenerContainer != null) {
            messageListenerContainer.destroy();
        }
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration() {
        CachingConnectionFactory testConnectionFactory = createConnectionFactory();
        Connection connection = testConnectionFactory.createConnection();
        connection.close();
        testConnectionFactory.destroy();
    }

    /**
     * Receives messages from RabbitMQ if an {@link EventTrigger} has been registered for this connector.
     *
     * @param message The RabbitMQ message.
     * @param channel The RabbitMQ channel.
     */
    @Override
    public void onMessage(org.springframework.amqp.core.Message message, Channel channel) {
        if (message.getBody() == null) {
            throw new IgorException("Received empty message from RabbitMQ!");
        }
        if (message.getMessageProperties() == null) {
            throw new IgorException("Received invalid message from RabbitMQ: message properties missing!");
        }

        String messageContent = new String(message.getBody());
        log.debug("Received message from RabbitMQ:\n{}", messageContent);

        Object parsed = JSONValue.parse(messageContent);
        Map<String, Object> metaData = new HashMap<>();
        metaData.put("deliveryTag", message.getMessageProperties().getDeliveryTag());
        message.getMessageProperties().getHeaders().forEach(metaData::put);

        Map<String, Object> dataItem = new HashMap<>();
        dataItem.put("message", parsed);
        dataItem.put("messageMeta", metaData);

        channels.put(message.getMessageProperties().getDeliveryTag(), channel);

        applicationEventPublisher.publishEvent(new JobTriggerEvent(jobId, dataItem, EventType.MESSAGE));
    }

    /**
     * Acknowledges the message at the RabbitMQ server as soon as the data item has been processed.
     *
     * @param dataItem The data item representing the received message.
     */
    @Override
    public void processingFinished(Map<String, Object> dataItem) {
        try {
            Long deliveryTag = JsonPath.parse(dataItem).read("$.data.messageMeta.deliveryTag", Long.class);
            if (deliveryTag != null && channels.containsKey(deliveryTag)) {
                channels.get(deliveryTag).basicAck(deliveryTag, false);
                channels.remove(deliveryTag);
            }
        } catch (PathNotFoundException | IOException e) {
            throw new IgorException("Could not ACK message from RabbitMQ: " + dataItem);
        }
    }

    /**
     * Creates a connection factory to the RabbitMQ server.
     *
     * @return A newly created {@link CachingConnectionFactory}.
     */
    private CachingConnectionFactory createConnectionFactory() {
        if (!StringUtils.hasText(host) || !StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IgorException("Connector configuration missing required values!");
        }
        CachingConnectionFactory result = new CachingConnectionFactory(host, port);
        result.setUsername(username);
        result.setPassword(password);
        result.setRequestedHeartBeat(heartBeat);
        result.setVirtualHost(virtualHost);
        result.setConnectionTimeout(connectionTimeout);
        return result;
    }

}
