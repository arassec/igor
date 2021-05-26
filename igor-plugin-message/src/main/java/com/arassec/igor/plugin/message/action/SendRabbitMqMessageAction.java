package com.arassec.igor.plugin.message.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.plugin.core.CorePluginUtils;
import com.arassec.igor.plugin.core.message.action.BaseMessageAction;
import com.arassec.igor.plugin.message.MessagePluginType;
import com.arassec.igor.plugin.message.connector.RabbitMqMessage;
import com.arassec.igor.plugin.message.connector.RabbitMqMessageConnector;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Sends a message to the specified messaging connector.
 */
@Slf4j
@Getter
@Setter
@IgorComponent
public class SendRabbitMqMessageAction extends BaseMessageAction {

    /**
     * The connector to use for message sending.
     */
    @NotNull
    @IgorParam
    private RabbitMqMessageConnector messageConnector;

    /**
     * The exchange to send messages to.
     */
    @NotEmpty
    @IgorParam
    private String exchange;

    /**
     * The message template to use.
     */
    @NotEmpty
    @IgorParam(subtype = ParameterSubtype.MULTI_LINE)
    private String messageTemplate;

    /**
     * The message encoding.
     */
    @NotEmpty
    @IgorParam(advanced = true)
    private String contentEncoding = "UTF-8";

    /**
     * The message's content type.
     */
    @NotEmpty
    @IgorParam(advanced = true)
    private String contentType = "text/plain";

    /**
     * Optional message headers.
     */
    @IgorParam(advanced = true, subtype = ParameterSubtype.MULTI_LINE)
    private String headers;

    /**
     * Only sends messages during simulated job executions if set to {@code false}.
     */
    @IgorParam(advanced = true, defaultValue = "true")
    private boolean simulationSafe;

    /**
     * Creates a new component instance.
     */
    public SendRabbitMqMessageAction() {
        super(MessagePluginType.SEND_RABBITMQ_MESSAGE_ACTION.getId());
        messageConnector = new RabbitMqMessageConnector(null);
        messageTemplate = "";
    }

    /**
     * Processes the supplied data and replaces variables from a message template with the values from the data. The resulting
     * message is sent via a message connector.
     *
     * @param data         The data the action will work with.
     * @param jobExecution The job execution log.
     *
     * @return The manipulated data.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {

        String content = CorePluginUtils.getString(data, messageTemplate);

        RabbitMqMessage message = new RabbitMqMessage();
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
            messageConnector.sendMessage(exchange, message);
        }

        log.trace("RabbitMqMessage sent:\n{}", content);

        return List.of(data);
    }

}
