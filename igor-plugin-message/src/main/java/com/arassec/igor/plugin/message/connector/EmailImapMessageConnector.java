package com.arassec.igor.plugin.message.connector;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.annotation.IgorSimulationSafe;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.CoreCategory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.*;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * <h2>'E-Mail Receiver (IMAP)' Connector</h2>
 *
 * <h3>Description</h3>
 * A connector that uses the IMAP protocol to receive mails.
 */
@Slf4j
@Getter
@Setter
@IgorComponent(typeId = "email-imap-message-connector", categoryId = CoreCategory.MESSAGE)
public class EmailImapMessageConnector extends EmailBaseConnector {

    /**
     * Prefix for mail configuration properties.
     */
    private static final String MAIL_PROPERTY_PREFIX = "mail.";

    /**
     * Jackson's ObjectMapper for JSON handling.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * The port, the IMAP service is listening on.
     */
    @Positive
    @IgorParam(sortIndex = 2)
    private int port = 993;

    /**
     * Tests the connector's configuration.
     */
    @Override
    public void testConfiguration() {
        var session = createSession();
        try {
            session.getStore().connect();
        } catch (MessagingException e) {
            throw new IgorException("Could not connect to E-Mail server!", e);
        }
    }

    /**
     * Retrieves E-Mails from the configured server.
     *
     * @param emailFolder             The folder to retrieve E-Mails from (e.g. 'INBOX').
     * @param onlyNew                 Set to {@code true} to retrieve only mails that haven't been read.
     * @param deleteProcessed         Set to {@code true} to delete any processed mail.
     * @param markReadAfterProcessing Set to {@code true} to mark processed mails as read.
     * @param saveAttachments         Set to {@code true} to save mail attachments to the local filesystem.
     * @param attachmentDirectory     The directory to store mail attachments in.
     * @return List of E-Mails including body and header data as JSON.
     * @throws MessagingException In case of exceptions during mail handling.
     */
    @IgorSimulationSafe
    public List<Map<String, Object>> retrieveEmails(String emailFolder, boolean onlyNew, boolean deleteProcessed,
                                                    boolean markReadAfterProcessing, boolean saveAttachments,
                                                    String attachmentDirectory) throws MessagingException {
        var protocol = "imap";
        if (enableTls) {
            protocol = "imaps";
        }

        if (StringUtils.hasText(attachmentDirectory) && !attachmentDirectory.endsWith(System.getProperty("file.separator"))) {
            attachmentDirectory = attachmentDirectory + System.getProperty("file.separator");
        }

        if (saveAttachments && !StringUtils.hasText(attachmentDirectory)) {
            throw new IgorException("No directory configured to save attachments in!");
        }

        Store store = null;
        Folder folder = null;
        try {
            List<Map<String, Object>> result = new LinkedList<>();

            var session = createSession();

            store = session.getStore(protocol);
            store.connect(username, password);

            folder = store.getFolder(emailFolder);
            folder.open(Folder.READ_WRITE);

            Message[] messages = folder.getMessages();

            for (Message message : messages) {
                if (onlyNew && message.isSet(Flags.Flag.SEEN)) {
                    continue;
                }
                Map<String, Object> messageJson = new HashMap<>();
                processMessageParts(message, messageJson, saveAttachments, attachmentDirectory);
                result.add(messageJson);
                if (deleteProcessed) {
                    message.setFlag(Flags.Flag.DELETED, true);
                } else {
                    message.setFlag(Flags.Flag.SEEN, markReadAfterProcessing);
                }
            }

            return result;
        } catch (IOException e) {
            throw new IgorException("Could not process incoming E-Mails!", e);
        } finally {
            if (folder != null) {
                folder.close(true);
            }
            if (store != null) {
                store.close();
            }
        }
    }

    /**
     * Creates the session required to access the mail service.
     *
     * @return A newly created {@link Session}.
     */
    private Session createSession() {
        var protocol = "imap";
        if (enableTls) {
            protocol = "imaps";
        }

        var properties = new Properties();
        properties.put(MAIL_PROPERTY_PREFIX + "store.protocol", protocol);
        properties.put(MAIL_PROPERTY_PREFIX + protocol + ".auth", true);
        properties.put(MAIL_PROPERTY_PREFIX + protocol + ".host", host);
        properties.put(MAIL_PROPERTY_PREFIX + protocol + ".port", String.valueOf(port));
        properties.put(MAIL_PROPERTY_PREFIX + protocol + ".starttls.enable", String.valueOf(enableTls));
        properties.put(MAIL_PROPERTY_PREFIX + protocol + ".timeout", readTimeout);
        properties.put(MAIL_PROPERTY_PREFIX + protocol + ".connectiontimeout", connectionTimeout);
        if (alwaysTrustSsl) {
            properties.put(MAIL_PROPERTY_PREFIX + protocol + ".ssl.trust", host);
        }

        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    /**
     * Processes message parts and handles attachments, sub-messages and message bodies.
     *
     * @param part                The message part to process.
     * @param targetJson          The JSON object to store processed data in (e.g. headers, bodies, ...).
     * @param saveAttachments     Set to {@code true} to save mail attachments.
     * @param attachmentDirectory The directory to save attachments in.
     * @throws MessagingException In case of errors during message processing.
     * @throws IOException        In case of errors during attachment processing.
     */
    @SuppressWarnings({"unchecked", "javasecurity:S2083"})
    private void processMessageParts(Part part, Map<String, Object> targetJson, boolean saveAttachments, String attachmentDirectory)
        throws MessagingException, IOException {
        if (part instanceof Message message) {
            if (message.getReplyTo().length >= 1) {
                targetJson.put("from", message.getReplyTo()[0].toString());
            } else if (message.getFrom().length >= 1) {
                targetJson.put("from", message.getFrom()[0].toString());
            }
            targetJson.put("recipients",
                Arrays.stream(message.getAllRecipients()).map(Address::toString).toList());
            Map<String, Object> headers = new HashMap<>();
            Enumeration<Header> allHeaders = message.getAllHeaders();
            while (allHeaders.hasMoreElements()) {
                var header = allHeaders.nextElement();
                headers.put(header.getName(), header.getValue());
            }
            targetJson.put("headers", headers);
            targetJson.put("bodies", new LinkedList<Map<String, Object>>());
        }

        Object content = part.getContent();

        if (content instanceof Multipart multipart) {
            int count = multipart.getCount();
            for (var i = 0; i < count; i++) {
                processMessageParts(multipart.getBodyPart(i), targetJson, saveAttachments, attachmentDirectory);
            }
        } else if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) && saveAttachments && StringUtils.hasText(attachmentDirectory)
            && Files.isWritable(Paths.get(attachmentDirectory, part.getFileName()))) {
            // It is the job admin's responsibility to configure the job in a way, that path
            // traversal attacks are prevented, e.g. by configuring a fixed 'attachamentDirectory' as apposed to a template.
            // Hence, the suppressed warning ("javasecurity:S2083")...
            try (var fos = new FileOutputStream(attachmentDirectory + part.getFileName())) {
                FileCopyUtils.copy(part.getInputStream(), fos);
            }
        } else if (content instanceof String) {
            Map<String, Object> body = new HashMap<>();
            body.put("contentType", part.getContentType());
            body.put("content", content);
            ((List<Map<String, Object>>) targetJson.get("bodies")).add(body);
        }
    }

}
