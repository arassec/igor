package com.arassec.igor.module.message.action;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.module.message.service.Message;
import com.arassec.igor.module.message.service.MessageService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sends a message to the specified messaging service.
 */
@Slf4j
@IgorComponent("Send message")
public class SendMessageAction extends BaseMessageAction {

    /**
     * The service to use for message sending.
     */
    @IgorParam
    private MessageService messageService;

    /**
     * The message template to use.
     */
    @IgorParam(subtype = ParameterSubtype.MULTI_LINE)
    private String messageTemplate;

    /**
     * Pattern to extract variables from the message template.
     */
    private Pattern pattern = Pattern.compile("##(.*?)##");

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
