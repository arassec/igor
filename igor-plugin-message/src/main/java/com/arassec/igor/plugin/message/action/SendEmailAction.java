package com.arassec.igor.plugin.message.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.CorePluginUtils;
import com.arassec.igor.plugin.message.connector.EmailSmtpMessageConnector;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <h2>'Send E-Mail' Action</h2>
 *
 * <h3>Description</h3>
 * This action sends an E-Mail.
 *
 * <h3>Mustache Template Parameters</h3>
 * All of the action's parameters support mustache templates as input.
 * <p>
 * As example, let's assume the action operates on the following data item:
 * <pre><code>
 * {
 *   "contact": {
 *     "mail": "igor@arassec.com",
 *     "name": "Igor",
 *     ...
 *   },
 *   "meta": {
 *     "jobId": "1e91a654-ba8c-4c3a-afd0-932bd27d2888",
 *     "timestamp": 1587203554775
 *   }
 * }
 * </code></pre>
 * <p>
 * The action's parameters could be configured in the following way
 * <p>
 * To: <pre><code>{{contact.mail}}</code></pre>
 * <p>
 * Body: <pre><code>Dear {{ contact.name }},\n...</code></pre>
 * <p>
 * to send an E-Mail based on the data item's input:
 *
 * <pre><code>
 * To: igor@arassec.com
 *
 * Dear igor,
 * ...
 * </code></pre>
 */
@Getter
@Setter
@IgorComponent(typeId = "send-email-message-action", categoryId = CoreCategory.MESSAGE)
public class SendEmailAction extends BaseAction {

    /**
     * The E-Mail SMTP connector to use for sending the mail.
     */
    @NotNull
    @IgorParam
    private EmailSmtpMessageConnector emailConnector;

    /**
     * The sender's E-Mail address.
     */
    @NotEmpty
    @Email
    @IgorParam
    private String from;

    /**
     * A comma separated list of recipients.
     */
    @NotEmpty
    @Email
    @IgorParam
    private String to;

    /**
     * The E-Mails subject.
     */
    @NotEmpty
    @IgorParam
    private String subject;

    /**
     * The E-Mail body's content type.
     */
    @NotEmpty
    @IgorParam
    private String contentType = "text/plain";

    /**
     * The E-Mail's body.
     */
    @NotEmpty
    @IgorParam(subtype = ParameterSubtype.MULTI_LINE)
    private String body;

    /**
     * List of newline separated paths to files that should be sent as E-Mail attachments.
     */
    @IgorParam(advanced = true, subtype = ParameterSubtype.MULTI_LINE)
    private String attachments;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        var fromResolved = CorePluginUtils.getString(data, from);
        var toResolved = CorePluginUtils.getString(data, to);
        var subjectResolved = CorePluginUtils.getString(data, subject);
        var contentTypeResolved = CorePluginUtils.getString(data, contentType);
        var bodyResolved = CorePluginUtils.getString(data, body);
        var attachmentsResolved = CorePluginUtils.getString(data, attachments);

        List<String> attachmentsPrepared = new LinkedList<>();
        if (StringUtils.hasText(attachmentsResolved)) {
            attachmentsPrepared = Arrays.stream(attachmentsResolved.split("\n"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }

        emailConnector.sendMessage(fromResolved, toResolved, subjectResolved, bodyResolved, contentTypeResolved, attachmentsPrepared);

        return List.of(data);
    }

}
