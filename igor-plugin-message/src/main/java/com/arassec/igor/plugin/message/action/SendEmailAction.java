package com.arassec.igor.plugin.message.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.plugin.core.CorePluginUtils;
import com.arassec.igor.plugin.core.message.action.BaseMessageAction;
import com.arassec.igor.plugin.message.MessagePluginType;
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
 * Action to send E-Mails.
 */
@Getter
@Setter
@IgorComponent
public class SendEmailAction extends BaseMessageAction {

    /**
     * The connector for mail sending.
     */
    @NotNull
    @IgorParam
    private EmailSmtpMessageConnector emailConnector;

    /**
     * The sender's mail address.
     */
    @NotEmpty
    @Email
    @IgorParam
    private String from;

    /**
     * Comma separated list of mail recipients.
     */
    @NotEmpty
    @Email
    @IgorParam
    private String to;

    /**
     * The mail's subject.
     */
    @NotEmpty
    @IgorParam
    private String subject;

    /**
     * The body's content type.
     */
    @NotEmpty
    @IgorParam(defaultValue = "text/plain")
    private String contentType;

    /**
     * The mail's body.
     */
    @NotEmpty
    @IgorParam(subtype = ParameterSubtype.MULTI_LINE)
    private String body;

    /**
     * List of newline separated paths to files that should be sent as mail attachment.
     */
    @IgorParam(advanced = true, subtype = ParameterSubtype.MULTI_LINE)
    private String attachments;

    /**
     * Creates a new component instance.
     */
    protected SendEmailAction() {
        super(MessagePluginType.SEND_EMAIL_MESSAGE_ACTION.getId());
    }

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
