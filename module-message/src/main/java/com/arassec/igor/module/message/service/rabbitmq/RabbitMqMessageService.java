package com.arassec.igor.module.message.service.rabbitmq;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.misc.ParameterSubtype;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.module.message.service.BaseMessageService;
import com.arassec.igor.module.message.service.Message;
import com.arassec.igor.module.message.service.MessageService;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * {@link MessageService} to process messages via RabbitMQ.
 */
@IgorService(label = "RabbitMQ")
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
     * The RabbitMQ-Client to send messages.
     */
    private RabbitTemplate rabbitTemplate;

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(Message message) {
        if (rabbitTemplate == null) {
            initialize();
        }

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
     * Initializes the RabbitMQ-Client with the provided configuration data.
     */
    private void initialize() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setRequestedHeartBeat(heartBeat);
        connectionFactory.setVirtualHost(virtualHost);

        rabbitTemplate = new RabbitTemplate(connectionFactory);
    }

}
