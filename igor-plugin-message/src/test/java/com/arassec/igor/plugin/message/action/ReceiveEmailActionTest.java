package com.arassec.igor.plugin.message.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.message.connector.EmailImapMessageConnector;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ReceiveEmailAction}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("'Receive E-Mail' action tests.")
class ReceiveEmailActionTest extends MessageActionBaseTest {

    /**
     * Tests receiving mails.
     */
    @Test
    @SneakyThrows
    @SuppressWarnings("unchecked")
    @DisplayName("Tests receiving mails.")
    void testProcess() {
        EmailImapMessageConnector connector = mock(EmailImapMessageConnector.class);

        when(connector.retrieveEmails("SPAM", true, true, false, true, "/attachment/directory"))
            .thenReturn(List.of(Map.of("mail-data", "mail-json")));

        ReceiveEmailAction action = new ReceiveEmailAction();
        action.setEmailConnector(connector);
        action.setFolderName("{{folder-name}}");
        action.setOnlyNew(true);
        action.setDeleteProcessed(true);
        action.setSaveAttachments(true);
        action.setAttachmentDirectory("{{attachment-directory}}");

        Map<String, Object> data = createData();
        data.put("folder-name", "SPAM");
        data.put("attachment-directory", "/attachment/directory");

        List<Map<String, Object>> result = action.process(data, new JobExecution());
        assertEquals(1, result.size());
        assertEquals("mail-json", ((Map<String, Object>) result.getFirst().get("message")).get("mail-data"));
    }

}
