package com.arassec.igor.plugin.message.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.CorePluginUtils;
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
 * <h1>'Receive E-Mail' Action</h1>
 *
 * <h2>Description</h2>
 * This action receives E-Mails and creates a new data item for every received E-Mail.
 *
 * <h2>Example</h2>
 * For every data item processed by the action, E-Mails are retrieved from the configured server.<br>
 * <p>
 * The created new data items contain the E-Mails data, e.g.:
 * <pre><code>
 * {
 *   "data": {
 *     ...
 *   },
 *   "meta": {
 *     ...
 *   },
 *   "message": {
 *     "headers": {
 *       "Return-Path": "&lt;andreas.sensen@arassec.com&gt;",
 *       "Delivered-To": "igor@igor-test.dev",
 *       "From": "andreas.sensen@arassec.com",
 *       "To": "igor@igor-test.dev",
 *       "Subject": "Test E-Mail",
 *       "MIME-Version": "1.0",
 *       "Content-Type": "multipart/mixed"
 *     },
 *     "recipients": [
 *       "igor@igor-test.dev"
 *     ],
 *     "bodies": [
 *       {
 *         "contentType": "text/plain; charset=us-ascii",
 *         "content": "Just a simple test..."
 *       }
 *     ],
 *     "from": "andreas.sensen@arassec.com"
 *   }
 * }
 * </code></pre>
 */
@Getter
@Setter
@IgorComponent(typeId = "receive-email-message-action", categoryId = CoreCategory.MESSAGE)
public class ReceiveEmailAction extends BaseAction {

    /**
     * The E-Mail IMAP connector to use for receiving E-Mails.
     */
    @NotNull
    @IgorParam
    private EmailImapMessageConnector emailConnector;

    /**
     * The name of the IMAP folder to read mails from, e.g. 'INBOX'.
     */
    @NotEmpty
    @IgorParam
    private String folderName = "INBOX";

    /**
     * If checked, only new mails will be received by this action.
     */
    @IgorParam(advanced = true, sortIndex = 9)
    private boolean onlyNew = true;

    /**
     * If checked, received E-Mails will be deleted by the action.
     */
    @IgorParam(advanced = true, sortIndex = 10)
    private boolean deleteProcessed = false;

    /**
     * If checked, attachments of the received E-Mails are saved in the configured directory (see below).
     */
    @IgorParam(advanced = true, sortIndex = 11)
    private boolean saveAttachments = false;

    /**
     * The target directory for downloaded attachments.
     */
    @IgorParam(advanced = true, sortIndex = 12)
    private String attachmentDirectory;

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
