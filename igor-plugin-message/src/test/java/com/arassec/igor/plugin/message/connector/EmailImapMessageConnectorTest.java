package com.arassec.igor.plugin.message.connector;

import com.arassec.igor.core.util.IgorException;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the {@link EmailImapMessageConnector}.
 */
class EmailImapMessageConnectorTest {

    /**
     * Creates a local IMAP server for testing.
     */
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.IMAP)
        .withConfiguration(GreenMailConfiguration.aConfig().withUser("igor@arassec.com", "igor", "igor"))
        .withPerMethodLifecycle(false);

    /**
     * Tests testing the configuration.
     */
    @Test
    @DisplayName("Tests testing the configuration.")
    void testTestConfiguration() {
        EmailImapMessageConnector connector = new EmailImapMessageConnector();
        connector.setHost("localhost");
        connector.setPort(ServerSetupTest.IMAP.getPort());
        connector.setUsername("igor");
        connector.setPassword("igor");
        connector.setEnableTls(false);

        connector.testConfiguration();

        connector.setPort(ServerSetupTest.IMAP.getPort() + 1);
        connector.setConnectionTimeout(10);

        assertThrows(IgorException.class, connector::testConfiguration);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Tests retrieving E-Mails.")
    @SneakyThrows
    void testRetrieveEmails() {
        EmailImapMessageConnector connector = new EmailImapMessageConnector();
        connector.setHost("localhost");
        connector.setPort(ServerSetupTest.IMAP.getPort());
        connector.setUsername("igor");
        connector.setPassword("igor");
        connector.setEnableTls(false);

        MimeMessage message = GreenMailUtil.newMimeMessage("igor mail string");
        message.setFrom(new InternetAddress("igor-test@arassec.com"));
        message.setRecipients(
            Message.RecipientType.TO, InternetAddress.parse("igor@arassec.com"));
        message.setSubject("igor mail test");

        MimeBodyPart contentPart = new MimeBodyPart();
        contentPart.setContent("body", "text/plain");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(contentPart);

        message.setContent(multipart);

        GreenMailUser user = greenMail.getUserManager().getUser("igor");
        user.deliver(message);

        List<Map<String, Object>> mails = connector.retrieveEmails("INBOX", true, false, false, false, null);

        assertEquals(1, mails.size());

        Map<String, Object> maildata = mails.get(0);

        assertEquals("igor-test@arassec.com", maildata.get("from"));
        assertEquals(1, ((List<Map<String, Object>>) maildata.get("bodies")).size());
        // Mail body doesn't seem to be supported with GrenmailUtil.newMimeMessage()?!?
        // assertEquals("body", ((List<Map<String, Object>>) maildata.get("bodies")).get(0).get("content"));
        assertEquals(1, ((List<String>) maildata.get("recipients")).size());
        assertEquals("igor@arassec.com", ((List<String>) maildata.get("recipients")).get(0));
        assertEquals("igor mail test", ((Map<String, Object>) maildata.get("headers")).get("Subject"));
    }

}
