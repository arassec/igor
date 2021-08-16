package com.arassec.igor.plugin.message.connector;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.annotation.IgorSimulationSafe;
import com.arassec.igor.core.model.connector.BaseConnector;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.trigger.EventTrigger;
import com.arassec.igor.core.model.trigger.EventType;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.core.util.event.JobTriggerEvent;
import com.arassec.igor.plugin.core.CoreCategory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
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
 * <h1>RabbitMQ Connector</h1>
 *
 * <h2>Description</h2>
 * A message-connector that uses RabbitMQ to send messages.
 */
@Getter
@Setter
@Slf4j
@IgorComponent(typeId = "rabbitmq-message-connector", categoryId = CoreCategory.MESSAGE)
public class RabbitMqMessageConnector extends BaseConnector implements ChannelAwareMessageListener {

    /**
     * Publisher for events based on job changes.
     */
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Jackson's ObjectMapper for JSON handling.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * The host running RabbitMQ.
     */
    @NotBlank
    @IgorParam
    private String host;

    /**
     * The port, RabbitMQ is listening on.
     */
    @Positive
    @IgorParam
    private int port = 5672;

    /**
     * The RabbitMQ user's name.
     */
    @NotBlank
    @IgorParam
    private String username;

    /**
     * The password of the RabbitMQ user.
     */
    @NotBlank
    @IgorParam(secured = true)
    private String password;

    /**
     * The virtual host to use on the RabbitMQ server.
     */
    @NotBlank
    @IgorParam(advanced = true)
    private String virtualHost = "/";

    /**
     * RabbitMQ heart beat <strong>in seconds</strong> to use to check the connection to the server.
     */
    @Positive
    @IgorParam(advanced = true)
    private int heartBeat = 30;

    /**
     * A timeout <strong>in milliseconds</strong> after which a connection to the server will be aborted.
     */
    @Positive
    @IgorParam(advanced = true)
    private int connectionTimeout = 60000;

    /**
     * Number of messages that are fetched at once without waiting for acknowledgements of the previous messages.
     */
    @Positive
    @IgorParam(advanced = true)
    private int prefetchCount = 10;

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
    public void sendMessage(String exchange, String routingKey, RabbitMqMessage message) {
        if (!StringUtils.hasText(exchange)) {
            throw new IgorException("No RabbitMQ exchange configured to send messages to!");
        }

        if (message == null || !StringUtils.hasText(message.getContent())) {
            throw new IgorException("Empty content provided for message sending!");
        }

        var messageProperties = new MessageProperties();
        messageProperties.setContentEncoding(message.getContentEncoding());
        messageProperties.setContentType(message.getContentType());

        message.getHeaders().forEach((key, value) -> messageProperties.getHeaders().put(key, value));

        var rabbitMessage = new org.springframework.amqp.core.Message(message.getContent().getBytes(), messageProperties);
        rabbitTemplate.send(exchange, routingKey, rabbitMessage);
    }

    /**
     * {@inheritDoc}
     */
    @IgorSimulationSafe
    public void enableMessageRetrieval(String queue) {
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
        var connection = testConnectionFactory.createConnection();
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

        var messageContent = new String(message.getBody());
        log.debug("Received message from RabbitMQ:\n{}", messageContent);

        Map<String, Object> metaData = new HashMap<>();
        metaData.put("deliveryTag", message.getMessageProperties().getDeliveryTag());
        message.getMessageProperties().getHeaders().forEach(metaData::put);

        Map<String, Object> dataItem = new HashMap<>();
        try {
            var jsonNode = objectMapper.readTree(messageContent);
            dataItem.put("message", objectMapper.convertValue(jsonNode, new TypeReference<Map<String, Object>>() {
            }));
        } catch (JsonProcessingException e) {
            dataItem.put("message", messageContent);
        }

        dataItem.put("messageMeta", metaData);

        channels.put(message.getMessageProperties().getDeliveryTag(), channel);

        applicationEventPublisher.publishEvent(new JobTriggerEvent(jobId, dataItem, EventType.MESSAGE));
    }

    /**
     * Acknowledges the message at the RabbitMQ server as soon as the data item has been processed.
     *
     * @param dataItem The data item representing the received message.
     */
    @IgorSimulationSafe
    public void processingFinished(Map<String, Object> dataItem) {
        try {
            var jsonNode = objectMapper.convertValue(dataItem, JsonNode.class);
            var deliveryTagNode = jsonNode.at("/messageMeta/deliveryTag");
            Long deliveryTag = deliveryTagNode.asLong();
            if (channels.containsKey(deliveryTag)) {
                channels.get(deliveryTag).basicAck(deliveryTag, false);
                channels.remove(deliveryTag);
            }
        } catch (IllegalArgumentException | IOException e) {
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
        var result = new CachingConnectionFactory(host, port);
        result.setUsername(username);
        result.setPassword(password);
        result.setRequestedHeartBeat(heartBeat);
        result.setVirtualHost(virtualHost);
        result.setConnectionTimeout(connectionTimeout);
        return result;
    }

}
