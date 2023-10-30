package com.arassec.igor.plugin.message.connector;


import com.arassec.igor.core.util.IgorException;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link EmailSmtpMessageConnector}.
 */
class EmailSmtpMessageConnectorTest {

    /**
     * Creates a local SMTP server for testing.
     */
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
        .withConfiguration(GreenMailConfiguration.aConfig().withUser("igor", "igor"))
        .withPerMethodLifecycle(false);

    /**
     * Tests testing the configuration.
     */
    @Test
    @DisplayName("Tests testing the configuration.")
    void testTestConfiguration() {
        EmailSmtpMessageConnector connector = new EmailSmtpMessageConnector();
        connector.setHost("localhost");
        connector.setPort(ServerSetupTest.SMTP.getPort());
        connector.setUsername("igor");
        connector.setPassword("igor");

        connector.testConfiguration();

        connector.setPort(ServerSetupTest.SMTP.getPort() + 1);
        connector.setConnectionTimeout(10);

        assertThrows(IgorException.class, connector::testConfiguration);
    }

    /**
     * Tests sending an E-Mail.
     */
    @Test
    @DisplayName("Tests sending an E-Mail.")
    @SneakyThrows
    void testSendMessage() {
        EmailSmtpMessageConnector connector = new EmailSmtpMessageConnector();
        connector.setHost("localhost");
        connector.setPort(ServerSetupTest.SMTP.getPort());
        connector.setUsername("igor");
        connector.setPassword("igor");

        connector.sendMessage("igor@arassec.com", "a@b.com", "smtp-test", "smtp junit test", "text/plain", List.of());

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length);

        MimeMessage msg = receivedMessages[0];
        assertEquals("igor@arassec.com", msg.getFrom()[0].toString());
        assertEquals("a@b.com", msg.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("smtp-test", msg.getSubject());
        assertNotNull(msg.getContent());
    }

}
