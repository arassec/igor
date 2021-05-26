package com.arassec.igor.plugin.message.action;


import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.message.connector.EmailSmtpMessageConnector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link SendEmailAction}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("'Send E-Mail' action tests.")
class SendEmailActionTest extends MessageActionBaseTest {

    /**
     * Tests receiving mails.
     */
    @Test
    @DisplayName("Tests sending mails.")
    void testProcess() {
        EmailSmtpMessageConnector connector = mock(EmailSmtpMessageConnector.class);

        SendEmailAction action = new SendEmailAction();
        action.setEmailConnector(connector);
        action.setFrom("{{from}}");
        action.setTo("{{to}}");
        action.setSubject("{{subject}}");
        action.setContentType("{{contentType}}");
        action.setBody("{{body}}");
        action.setAttachments("{{attachments}}");

        Map<String, Object> data = createData();
        data.put("from", "igor@arassec.com");
        data.put("to", "a@b.com,c@d.org");
        data.put("subject", "igor-test");
        data.put("contentType", "text/plain");
        data.put("body", "Igor mail test!");
        data.put("attachments", "a.txt\nb.csv");

        List<Map<String, Object>> result = action.process(data, new JobExecution());

        assertEquals(1, result.size());
        assertEquals(data, result.get(0));

        verify(connector, times(1)).sendMessage("igor@arassec.com", "a@b.com,c@d.org", "igor-test", "Igor mail test!",
            "text/plain", List.of("a.txt", "b.csv"));
    }

}
