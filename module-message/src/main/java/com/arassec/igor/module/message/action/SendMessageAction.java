package com.arassec.igor.module.message.action;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.module.message.service.Message;
import com.arassec.igor.module.message.service.MessageService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sends a message to the specified messaging service.
 */
@Slf4j
@Getter
@Setter
@ConditionalOnBean(MessageService.class)
@IgorComponent
public class SendMessageAction extends BaseMessageAction {

    /**
     * The service to use for message sending.
     */
    @NotNull
    @IgorParam
    private MessageService messageService;

    /**
     * The message template to use.
     */
    @NotNull
    @IgorParam(subtype = ParameterSubtype.MULTI_LINE)
    private String messageTemplate;

    /**
     * Pattern to extract variables from the message template.
     */
    private final Pattern pattern = Pattern.compile("##(.*?)##");

    /**
     * Creates a new component instance.
     */
    public SendMessageAction() {
        super("88a0e988-d3ec-4b91-b98c-92d99c09ba33");
    }

    /**
     * Processes the supplied data and replaces variables from a message template with the values from the data. The resulting
     * message is sent via a message service.
     *
     * @param data         The data the action will work with.
     * @param jobExecution The job execution log.
     *
     * @return The manipulated data.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {

        String content = messageTemplate;

        Matcher m = pattern.matcher(content);
        while (m.find()) {
            // e.g. ##$.data.targetFilename##
            String variableName = m.group();
            // e.g. file.zip
            String variableContent = getString(data, variableName.replace("##", ""));
            if (variableContent != null) {
                content = content.replace(variableName, variableContent);
            }
        }

        Message message = new Message();
        message.setContent(content);
        messageService.sendMessage(message);
        log.debug("Message sent: '{}'", content);

        return List.of(data);
    }

}
