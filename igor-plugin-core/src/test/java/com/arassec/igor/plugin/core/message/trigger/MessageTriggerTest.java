package com.arassec.igor.plugin.core.message.trigger;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.trigger.EventType;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.message.connector.FallbackMessageConnector;
import com.arassec.igor.plugin.core.message.connector.MessageConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link MessageTrigger}.
 */
@DisplayName("'Message trigger' tests.")
class MessageTriggerTest {

    /**
     * The trigger under test.
     */
    private final MessageTrigger trigger = new MessageTrigger();

    /**
     * Mock of the message connector used in the trigger.
     */
    private MessageConnector messageConnectorMock;

    @BeforeEach
    void initialize() {
        messageConnectorMock = mock(MessageConnector.class);
        trigger.setMessageConnector(messageConnectorMock);
    }

    /**
     * Tests instance creation.
     */
    @Test
    @DisplayName("Tests instance creation.")
    void testInstanceCreation() {
        MessageTrigger messageTrigger = new MessageTrigger();
        assertEquals(CoreCategory.MESSAGE.getId(), messageTrigger.getCategoryId());
        assertTrue(messageTrigger.getMessageConnector() instanceof FallbackMessageConnector);
    }

    /**
     * Tests initialization.
     */
    @Test
    @DisplayName("Tests initialization.")
    void testInitialization() {
        trigger.initialize("job-id", new JobExecution());
        verify(messageConnectorMock, times(1)).enableMessageRetrieval();
    }

    /**
     * Tests handling of processed data items.
     */
    @Test
    @DisplayName("Tests handling of processed data items.")
    void testProcessingFinished() {
        Map<String, Object> dataItem = Map.of("a", "b");
        trigger.processingFinished(dataItem);
        verify(messageConnectorMock, times(1)).processingFinished(eq(dataItem));
    }

    /**
     * Tests the supported event type.
     */
    @Test
    @DisplayName("Tests the supported event type.")
    void testSupportedEventType() {
        assertEquals(EventType.MESSAGE, trigger.getSupportedEventType());
    }

}
