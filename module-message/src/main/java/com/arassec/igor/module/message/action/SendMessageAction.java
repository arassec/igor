package com.arassec.igor.module.message.action;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.module.message.connector.FallbackMessageConnector;
import com.arassec.igor.module.message.connector.Message;
import com.arassec.igor.module.message.connector.MessageConnector;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

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
@ConditionalOnBean(MessageConnector.class)
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
        message.setContent(content);
        messageConnector.sendMessage(message);
        log.debug("Message sent: '{}'", content);

        return List.of(data);
    }

}
