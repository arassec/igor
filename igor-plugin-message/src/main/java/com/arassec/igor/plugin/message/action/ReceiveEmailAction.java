package com.arassec.igor.plugin.message.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.CorePluginUtils;
import com.arassec.igor.plugin.core.message.action.BaseMessageAction;
import com.arassec.igor.plugin.message.MessagePluginType;
import com.arassec.igor.plugin.message.connector.EmailImapMessageConnector;
import lombok.Getter;
import lombok.Setter;

import javax.mail.MessagingException;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Action to receive E-Mails.
 */
@Getter
@Setter
@IgorComponent
public class ReceiveEmailAction extends BaseMessageAction {

    /**
     * The connector to receive mails from.
     */
    @NotNull
    @IgorParam
    private EmailImapMessageConnector emailConnector;

    /**
     * The E-Mail folder to read mails from.
     */
    @NotEmpty
    @IgorParam
    private String folderName = "INBOX";

    /**
     * Set to {@code true} if only new mails should be processed.
     */
    @IgorParam(advanced = true, sortIndex = 9)
    private boolean onlyNew = true;

    /**
     * Set to {@code true} if mails should be deleted after processing.
     */
    @IgorParam(advanced = true, sortIndex = 10)
    private boolean deleteProcessed = false;

    /**
     * Set to {@code true} if mail attachments should be saved.
     */
    @IgorParam(advanced = true, sortIndex = 11)
    private boolean saveAttachments = false;

    /**
     * The directory in the local filesystem to store mails in.
     */
    @IgorParam(advanced = true, sortIndex = 12)
    private String attachmentDirectory;

    /**
     * Creates a new component instance.
     */
    public ReceiveEmailAction() {
        super(MessagePluginType.RECEIVE_EMAIL_MESSAGE_ACTION.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        List<Map<String, Object>> result = new LinkedList<>();

        var folder = CorePluginUtils.getString(data, folderName);
        var attachmentTargetDirectory = CorePluginUtils.getString(data, attachmentDirectory);

        boolean markReadAfterProcessing = !deleteProcessed;
        if (isSimulation(data)) {
            markReadAfterProcessing = false;
        }

        try {
            List<Map<String, Object>> messageJsons = emailConnector.retrieveEmails(folder, onlyNew, deleteProcessed,
                markReadAfterProcessing, saveAttachments, attachmentTargetDirectory);
            messageJsons.forEach(messageJson -> {
                Map<String, Object> dataClone = CorePluginUtils.clone(data);
                dataClone.put("message", messageJson);
                result.add(dataClone);
            });
        } catch (MessagingException e) {
            throw new IgorException("Could not retrieve E-Mails from server!", e);
        }

        return result;
    }

}
