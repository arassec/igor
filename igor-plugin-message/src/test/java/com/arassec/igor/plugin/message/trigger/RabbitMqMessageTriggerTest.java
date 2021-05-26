package com.arassec.igor.plugin.message.trigger;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.trigger.EventType;
import com.arassec.igor.plugin.core.CorePluginCategory;
import com.arassec.igor.plugin.message.connector.RabbitMqMessageConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link RabbitMqMessageTrigger}.
 */
@DisplayName("'RabbitMqMessage trigger' tests.")
class RabbitMqMessageTriggerTest {

    /**
     * The trigger under test.
     */
    private final RabbitMqMessageTrigger trigger = new RabbitMqMessageTrigger();

    /**
     * Mock of the message connector used in the trigger.
     */
    private RabbitMqMessageConnector messageConnectorMock;

    @BeforeEach
    void initialize() {
        messageConnectorMock = mock(RabbitMqMessageConnector.class);
        trigger.setRabbitMqConnector(messageConnectorMock);
    }

    /**
     * Tests instance creation.
     */
    @Test
    @DisplayName("Tests instance creation.")
    void testInstanceCreation() {
        RabbitMqMessageTrigger messageTrigger = new RabbitMqMessageTrigger();
        assertEquals(CorePluginCategory.MESSAGE.getId(), messageTrigger.getCategoryId());
        assertNotNull(messageTrigger.getRabbitMqConnector());
    }

    /**
     * Tests initialization.
     */
    @Test
    @DisplayName("Tests initialization.")
    void testInitialization() {
        trigger.setQueueName("test-queue");
        trigger.initialize(new JobExecution());
        verify(messageConnectorMock, times(1)).enableMessageRetrieval("test-queue");
    }

    /**
     * Tests handling of processed data items.
     */
    @Test
    @DisplayName("Tests handling of processed data items.")
    void testProcessingFinished() {
        Map<String, Object> dataItem = Map.of("a", "b");
        trigger.processingFinished(dataItem);
        verify(messageConnectorMock, times(1)).processingFinished(dataItem);
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
