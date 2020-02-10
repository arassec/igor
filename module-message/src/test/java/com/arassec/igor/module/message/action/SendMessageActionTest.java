package com.arassec.igor.module.message.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.message.service.Message;
import com.arassec.igor.module.message.service.MessageService;
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
     * Tests processing the action with JSON-Path parameters.
     */
    @Test
    @DisplayName("Tests the action with JSON-Path parameters.")
    void testProcess() {
        MessageService messageServiceMock = mock(MessageService.class);

        SendMessageAction action = new SendMessageAction();
        action.setMessageService(messageServiceMock);
        action.setMessageTemplate("{'key': '##$.data." + PARAM_KEY + "##'}");

        ArgumentCaptor<Message> argCap = ArgumentCaptor.forClass(Message.class);

        List<Map<String, Object>> result = action.process(createData(), new JobExecution());

        verify(messageServiceMock, times(1)).sendMessage(argCap.capture());

        assertEquals(1, result.size());
        assertEquals("{'key': '" + PARAM_VALUE + "'}", argCap.getValue().getContent());
    }

}
