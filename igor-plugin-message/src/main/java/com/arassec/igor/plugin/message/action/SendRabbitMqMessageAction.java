package com.arassec.igor.plugin.message.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.CorePluginUtils;
import com.arassec.igor.plugin.message.MessageType;
import com.arassec.igor.plugin.message.connector.RabbitMqMessage;
import com.arassec.igor.plugin.message.connector.RabbitMqMessageConnector;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * <h2>'Send RabbitMQ Message' Action</h2>
 *
 * <h3>Description</h3>
 * This action sends a message to a RabbitMQ exchange.
 *
 * <h3>Mustache Template Parameters</h3>
 * The message template can contain mustache expressions to fill the message with dynamic values from the processed data
 * item.<br>
 * <p>
 * As example, let's assume the action operates on the following data item:
 * <pre><code>
 * {
 *   "data": {
 *     "filename": "README.TXT",
 *     "lastModified": "2020-04-18T00:00:00+02:00",
 *     "directory": "/"
 *   },
 *   "meta": {
 *     "jobId": "1e91a654-ba8c-4c3a-afd0-932bd27d2888",
 *     "timestamp": 1587203554775
 *   }
 * }
 * </code></pre>
 * <p>
 * The action should send a message containing the filename with the key 'file':
 * <pre><code>
 * {
 *   "file": "README.TXT"
 * }
 * </code></pre>
 * <p>
 * This can be done by configuring the following message template:
 * <pre><code>
 * {
 *   "file": "{{data.filename}}"
 * }
 * </code></pre>
 */
@Slf4j
@Getter
@Setter
@IgorComponent(categoryId = CoreCategory.MESSAGE, typeId = MessageType.SEND_RABBITMQ_ACTION)
public class SendRabbitMqMessageAction extends BaseAction {

    /**
     * The RabbitMQ connector to use for sending the message.
     */
    @NotNull
    @IgorParam
    private RabbitMqMessageConnector messageConnector;

    /**
     * The RabbitMQ exchange to send the messages to.
     */
    @NotEmpty
    @IgorParam
    private String exchange;

    /**
     * A template message that is used as message body. Parameters can be filled by using the template syntax explained below.
     */
    @NotEmpty
    @IgorParam(subtype = ParameterSubtype.MULTI_LINE)
    private String messageTemplate;

    /**
     * Sets the encoding for messages sent by this connector.
     */
    @NotEmpty
    @IgorParam(advanced = true)
    private String contentEncoding = "UTF-8";

    /**
     * Specifies the content type of the message's content.
     */
    @NotEmpty
    @IgorParam(advanced = true)
    private String contentType = "text/plain";

    /**
     * An optional routing key that will be used in messages of this connector.
     */
    @IgorParam(advanced = true)
    private String routingKey;

    /**
     * Each line can contain a 'Header:Value'-pair which is used in messages sent by this action.
     */
    @IgorParam(advanced = true, subtype = ParameterSubtype.MULTI_LINE)
    private String headers;

    /**
     * Only sends messages during simulated job executions if unchecked.
     */
    @IgorParam(advanced = true)
    private boolean simulationSafe = true;

    /**
     * Creates a new component instance.
     */
    public SendRabbitMqMessageAction() {
        messageConnector = new RabbitMqMessageConnector(null);
        messageTemplate = "";
    }

    /**
     * Processes the supplied data and replaces variables from a message template with the values from the data. The resulting
     * message is sent via a message connector.
     *
     * @param data         The data the action will work with.
     * @param jobExecution The job execution log.
     * @return The manipulated data.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {

        var content = CorePluginUtils.evaluateTemplate(data, messageTemplate);

        var message = new RabbitMqMessage();
        message.setContentType(contentType);
        message.setContentEncoding(contentEncoding);
        message.setContent(content);

        if (StringUtils.hasText(headers)) {
            String[] separatedHeaders = headers.split("\n");
            for (String header : separatedHeaders) {
                String[] headerParts = header.split(":");
                if (headerParts.length == 2) {
                    message.getHeaders().put(headerParts[0].trim(), headerParts[1].trim());
                }
            }
        }

        if (!isSimulation(data) || !simulationSafe) {
            messageConnector.sendMessage(exchange, routingKey, message);
        }

        log.trace("RabbitMqMessage sent:\n{}", content);

        return List.of(data);
    }

}
