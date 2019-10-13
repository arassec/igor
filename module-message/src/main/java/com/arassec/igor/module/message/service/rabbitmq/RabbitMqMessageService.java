package com.arassec.igor.module.message.service.rabbitmq;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.module.message.service.BaseMessageService;
import com.arassec.igor.module.message.service.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Message service to process messages via RabbitMQ.
 */
@Component
@Scope("prototype")
@ConditionalOnClass(RabbitTemplate.class)
public class RabbitMqMessageService extends BaseMessageService {

    /**
     * The RabbitMQ host.
     */
    @IgorParam
    private String host;

    /**
     * The RabbitMQ port.
     */
    @IgorParam
    private int port = 5672;

    /**
     * The RabbitMQ username.
     */
    @IgorParam
    private String username;

    /**
     * The RabbitMQ password.
     */
    @IgorParam(secured = true)
    private String password;

    /**
     * The exchange messages are sent to.
     */
    @IgorParam
    private String exchange;

    /**
     * The optional routing key for messages.
     */
    @IgorParam(optional = true)
    private String routingKey;

    /**
     * The virtual host.
     */
    @IgorParam(optional = true)
    private String virtualHost = "/";

    /**
     * The message encoding.
     */
    @IgorParam(optional = true)
    private String contentEncoding = "UTF-8";

    /**
     * The message's content type.
     */
    @IgorParam(optional = true)
    private String contentType = "application/json";

    /**
     * Optional message headers.
     */
    @IgorParam(optional = true, subtype = ParameterSubtype.MULTI_LINE)
    private String headers;

    /**
     * Optional heartbeat for connections to the RabbitMQ server.
     */
    @IgorParam(optional = true)
    private int heartBeat = 30;

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(Message message) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setRequestedHeartBeat(heartBeat);
        connectionFactory.setVirtualHost(virtualHost);

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        try {
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
        } finally {
            rabbitTemplate.stop();
            connectionFactory.destroy();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration() throws ServiceException {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setRequestedHeartBeat(heartBeat);
        connectionFactory.setVirtualHost(virtualHost);

        Connection connection = connectionFactory.createConnection();
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeId() {
        return "481df4ed-008f-4d6e-a186-76b71325f362";
    }

}
