package com.arassec.igor.module.message.service.rabbitmq;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.module.message.service.BaseMessageService;
import com.arassec.igor.module.message.service.Message;
import lombok.Getter;
import lombok.Setter;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

/**
 * Message service to process messages via RabbitMQ.
 */
@Getter
@Setter
@ConditionalOnClass(RabbitTemplate.class)
@IgorComponent
public class RabbitMqMessageService extends BaseMessageService {

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
    @IgorParam
    private int port = 5672;

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
    @NotBlank
    @IgorParam
    private String exchange;

    /**
     * The optional routing key for messages.
     */
    @IgorParam(advanced = true)
    private String routingKey;

    /**
     * The virtual host.
     */
    @IgorParam(advanced = true)
    private String virtualHost = "/";

    /**
     * The message encoding.
     */
    @IgorParam(advanced = true)
    private String contentEncoding = "UTF-8";

    /**
     * The message's content type.
     */
    @IgorParam(advanced = true)
    private String contentType = "application/json";

    /**
     * Optional message headers.
     */
    @IgorParam(advanced = true, subtype = ParameterSubtype.MULTI_LINE)
    private String headers;

    /**
     * Optional heartbeat for connections to the RabbitMQ server.
     */
    @IgorParam(advanced = true)
    private int heartBeat = 30;

    /**
     * Optional connection timeout.
     */
    @IgorParam(advanced = true)
    private int connectionTimeout = 60000;

    /**
     * The connection factory to the RabbitMQ server.
     */
    private CachingConnectionFactory connectionFactory;

    /**
     * The RabbitMQ-Client.
     */
    private RabbitTemplate rabbitTemplate;

    /**
     * Creates a new component instance.
     */
    public RabbitMqMessageService() {
        super("481df4ed-008f-4d6e-a186-76b71325f362");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(String jobId, String taskId, JobExecution jobExecution) {
        super.initialize(jobId, taskId, jobExecution);

        connectionFactory = new CachingConnectionFactory(host, port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setRequestedHeartBeat(heartBeat);
        connectionFactory.setVirtualHost(virtualHost);
        connectionFactory.setConnectionTimeout(connectionTimeout);

        rabbitTemplate = new RabbitTemplate(connectionFactory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(Message message) {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentEncoding(contentEncoding);
        messageProperties.setContentType(contentType);
        if (headers != null) {
            String[] seperatedHeaders = headers.split("\n");
            for (String header : seperatedHeaders) {
                String[] headerParts = header.split(":");
                if (headerParts.length == 2) {
                    messageProperties.getHeaders().put(headerParts[0], headerParts[1]);
                }
            }
        }

        org.springframework.amqp.core.Message rabbitMessage = new org.springframework.amqp.core.Message(message.getContent().getBytes(), messageProperties);
        rabbitTemplate.send(exchange, routingKey, rabbitMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown(String jobId, String taskId, JobExecution jobExecution) {
        super.shutdown(jobId, taskId, jobExecution);
        rabbitTemplate.stop();
        connectionFactory.destroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration() {
        CachingConnectionFactory testConnectionFactory = new CachingConnectionFactory(host, port);
        testConnectionFactory.setUsername(username);
        testConnectionFactory.setPassword(password);
        testConnectionFactory.setRequestedHeartBeat(heartBeat);
        testConnectionFactory.setVirtualHost(virtualHost);
        testConnectionFactory.setConnectionTimeout(connectionTimeout);

        Connection connection = testConnectionFactory.createConnection();
        connection.close();
    }

}
