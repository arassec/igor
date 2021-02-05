package com.arassec.igor.plugin.core.message.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.message.connector.Message;
import com.arassec.igor.plugin.core.message.connector.MessageConnector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


/**
 * Tests the {@link SendMessageAction}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("'Send message' action tests.")
class SendMessageActionTest extends MessageActionBaseTest {

    /**
     * Tests processing the action with mustache template parameter.
     */
    @Test
    @DisplayName("Tests processing the action with mustache template parameter.")
    void testProcess() {
        MessageConnector messageConnectorMock = mock(MessageConnector.class);

        SendMessageAction action = new SendMessageAction();
        action.setMessageConnector(messageConnectorMock);
        action.setMessageTemplate("{'key': '{{data." + PARAM_KEY + "}}'}");

        ArgumentCaptor<Message> argCap = ArgumentCaptor.forClass(Message.class);

        List<Map<String, Object>> result = action.process(createData(), new JobExecution());

        verify(messageConnectorMock, times(1)).sendMessage(argCap.capture());

        assertEquals(1, result.size());
        assertEquals("{'key': '" + PARAM_VALUE + "'}", argCap.getValue().getContent());
    }

    /**
     * Tests processing the action with message headers.
     */
    @Test
    @DisplayName("Tests processing the action with message headers.")
    void testProcessHeaders() {
        MessageConnector messageConnectorMock = mock(MessageConnector.class);

        SendMessageAction action = new SendMessageAction();
        action.setMessageConnector(messageConnectorMock);
        action.setMessageTemplate("message-template");
        action.setHeaders("a: b\nc: d\ne\nf=g");


        ArgumentCaptor<Message> argCap = ArgumentCaptor.forClass(Message.class);

        List<Map<String, Object>> result = action.process(createData(), new JobExecution());

        verify(messageConnectorMock, times(1)).sendMessage(argCap.capture());
        assertEquals(1, result.size());

        Message sentMessage = argCap.getValue();
        assertEquals("b", sentMessage.getHeaders().get("a"));
        assertEquals("d", sentMessage.getHeaders().get("c"));
    }

}
