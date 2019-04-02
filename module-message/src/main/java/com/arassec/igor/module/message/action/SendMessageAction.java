package com.arassec.igor.module.message.action;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.misc.ParameterSubtype;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.module.message.service.Message;
import com.arassec.igor.module.message.service.MessageService;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sends a message to the specified messaging service.
 */
@Slf4j
@IgorAction(label = "Send message")
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
    private Pattern pattern = Pattern.compile("\\$(.*?)\\$");

    /**
     * @param data
     *         The data the action will work with.
     * @return
     */
    @Override
    public boolean process(IgorData data) {
        return processInternal(data, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean dryRun(IgorData data) {
        return processInternal(data, true);
    }

    /**
     * Sends a message by using the configured template and replacing variables with content from the supplied IgorData.
     *
     * @param data
     *         The data.
     * @param isDryRun
     *         Set to {@code true} if the message should not actually be sent, {@code false} otherwise.
     * @return {@code true} if the data should further be processed, {@code false} otherwise.
     */
    private boolean processInternal(IgorData data, boolean isDryRun) {

        String content = messageTemplate;

        Matcher m = pattern.matcher(content);
        while (m.find()) {
            // e.g. $targetFilename$
            String variableName = m.group();
            // e.g. file.zip
            Object variableContent = data.get(variableName.replace("$", ""));
            if (variableContent != null) {
                content.replace(variableName, String.valueOf(variableContent));
            }
        }

        if (isDryRun) {
            data.put(DRY_RUN_COMMENT_KEY, "sendMessage: " + content);
        } else {
            Message message = new Message();
            message.setContent(content);
            messageService.sendMessage(message);
        }

        return true;
    }

}
