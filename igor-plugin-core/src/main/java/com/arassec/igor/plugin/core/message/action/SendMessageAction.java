package com.arassec.igor.plugin.core.message.action;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.plugin.core.message.connector.FallbackMessageConnector;
import com.arassec.igor.plugin.core.message.connector.Message;
import com.arassec.igor.plugin.core.message.connector.MessageConnector;
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
public class SendMessageAction extends BaseMessageAction {

    /**
     * The connector to use for message sending.
     */
    @NotNull
    @IgorParam
    private MessageConnector messageConnector;

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
     * Creates a new component instance.
     */
    public SendMessageAction() {
        super("send-message-action");
        messageConnector = new FallbackMessageConnector();
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

        String content = getString(data, messageTemplate);

        Message message = new Message();
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

        messageConnector.sendMessage(message);

        log.trace("Message sent:\n{}", content);

        return List.of(data);
    }

}
