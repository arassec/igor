package com.arassec.igor.plugin.message.connector;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.CoreCategory;
import lombok.Getter;
import lombok.Setter;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.validation.constraints.Positive;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * <h1>'E-Mail Sender (SMTP)' Connector</h1>
 *
 * <h2>Description</h2>
 * A connector that uses the SMTP protocol to send E-Mails.
 */
@Getter
@Setter
@IgorComponent(typeId = "email-smtp-message-connector", categoryId = CoreCategory.MESSAGE)
public class EmailSmtpMessageConnector extends EmailBaseConnector {

    /**
     * The port of the SMTP service.
     */
    @Positive
    @IgorParam(sortIndex = 2)
    private int port = 25;

    /**
     * Tests the connector configuration.
     */
    @Override
    public void testConfiguration() {
        var session = createSession();
        try {
            session.getTransport("smtp").connect(host, username, password);
        } catch (MessagingException e) {
            throw new IgorException("Could not connect to E-Mail server!", e);
        }
    }

    /**
     * Sends an E-Mail with the supplied information.
     *
     * @param from        The sender address.
     * @param to          Comma separated list of recipients of the mail.
     * @param subject     The mail's subject.
     * @param body        The mail's body.
     * @param contentType The body's content type.
     * @param attachments List of paths to files that should be sent as attachment.
     */
    public void sendMessage(String from, String to, String subject, String body, String contentType, List<String> attachments) {
        try {
            Message message = new MimeMessage(createSession());
            message.setFrom(new InternetAddress(from));
            message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            var contentPart = new MimeBodyPart();
            contentPart.setContent(body, contentType);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(contentPart);

            attachments.forEach(attachment -> {
                var attachmentPart = new MimeBodyPart();
                try {
                    attachmentPart.attachFile(attachment);
                    multipart.addBodyPart(attachmentPart);
                } catch (MessagingException | IOException e) {
                    throw new IgorException("Could not add attachment to E-Mail message!", e);
                }
            });

            message.setContent(multipart);

            Transport.send(message);
        } catch (MessagingException e) {
            throw new IgorException("Could not send E-Mail!", e);
        }

    }

    /**
     * Creates a session to the SMTP service.
     *
     * @return A newly created {@link Session}.
     */
    private Session createSession() {
        var properties = new Properties();
        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", String.valueOf(port));
        properties.put("mail.smtp.starttls.enable", String.valueOf(enableTls));
        properties.put("mail.smtp.timeout", readTimeout);
        properties.put("mail.smtp.connectiontimeout", connectionTimeout);

        if (alwaysTrustSsl) {
            properties.put("mail.smtp.ssl.trust", host);
        }

        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

}
