package com.arassec.igor.plugin.message.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.message.connector.RabbitMqMessage;
import com.arassec.igor.plugin.message.connector.RabbitMqMessageConnector;
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
 * Tests the {@link SendRabbitMqMessageAction}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("'Send RabbitMQ message' action tests.")
class SendRabbitMqMessageActionTest extends MessageActionBaseTest {

    /**
     * Tests processing the action with mustache template parameter.
     */
    @Test
    @DisplayName("Tests processing the action with mustache template parameter.")
    void testProcess() {
        RabbitMqMessageConnector messageConnectorMock = mock(RabbitMqMessageConnector.class);

        SendRabbitMqMessageAction action = new SendRabbitMqMessageAction();
        action.setMessageConnector(messageConnectorMock);
        action.setExchange("test-exchange");
        action.setMessageTemplate("{'key': '{{data." + PARAM_KEY + "}}'}");

        ArgumentCaptor<RabbitMqMessage> argCap = ArgumentCaptor.forClass(RabbitMqMessage.class);

        List<Map<String, Object>> result = action.process(createData(), new JobExecution());

        verify(messageConnectorMock, times(1)).sendMessage(eq("test-exchange"), argCap.capture());

        assertEquals(1, result.size());
        assertEquals("{'key': '" + PARAM_VALUE + "'}", argCap.getValue().getContent());
    }

    /**
     * Tests processing the action with message headers.
     */
    @Test
    @DisplayName("Tests processing the action with message headers.")
    void testProcessHeaders() {
        RabbitMqMessageConnector messageConnectorMock = mock(RabbitMqMessageConnector.class);

        SendRabbitMqMessageAction action = new SendRabbitMqMessageAction();
        action.setMessageConnector(messageConnectorMock);
        action.setExchange("test-exchange");
        action.setMessageTemplate("message-template");
        action.setHeaders("a: b\nc: d\ne\nf=g");

        ArgumentCaptor<RabbitMqMessage> argCap = ArgumentCaptor.forClass(RabbitMqMessage.class);

        List<Map<String, Object>> result = action.process(createData(), new JobExecution());

        verify(messageConnectorMock, times(1)).sendMessage(eq("test-exchange"), argCap.capture());
        assertEquals(1, result.size());

        RabbitMqMessage sentMessage = argCap.getValue();
        assertEquals("b", sentMessage.getHeaders().get("a"));
        assertEquals("d", sentMessage.getHeaders().get("c"));
    }

}
