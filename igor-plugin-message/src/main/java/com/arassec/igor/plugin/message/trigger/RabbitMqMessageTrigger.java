package com.arassec.igor.plugin.message.trigger;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.trigger.BaseEventTrigger;
import com.arassec.igor.core.model.trigger.EventType;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.message.connector.RabbitMqMessageConnector;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * <h2>RabbitMQ Message Trigger</h2>
 *
 * <h3>Description</h3>
 * A trigger that fires on incoming messages on a RabbitMQ server.<br>
 * <p>
 * This message trigger is an event based trigger that processes an incoming message as data item as soon as it is received.
 *
 * <h3>Limitations and Caveats</h3>
 * Not all actions are available for event-triggered jobs. E.g. sorting by timestamp requires all data items, that should be
 * sorted, to be known to the action. Since event-triggered jobs process a continuous stream of incoming events, there is no fixed
 * number of data items to sort.
 * <p>
 * During simulated job executions the trigger will receive (and probably consume) messages provided by the message connector.
 * Thus RabbitMQ messages will be retrieved <strong>and acknowledged</strong> during simulated job executions!
 */
@Getter
@Setter
@IgorComponent(typeId = "rabbitmq-message-trigger", categoryId = CoreCategory.MESSAGE)
public class RabbitMqMessageTrigger extends BaseEventTrigger {

    /**
     * A RabbitMQ message connector that provides the incoming messages.
     */
    @NotNull
    @IgorParam
    private RabbitMqMessageConnector rabbitMqConnector;

    /**
     * The RabbitMQ queue containing the messages.
     */
    @NotEmpty
    @IgorParam
    private String queueName;

    /**
     * Creates a new component instance.
     */
    protected RabbitMqMessageTrigger() {
        rabbitMqConnector = new RabbitMqMessageConnector(null);
    }

    /**
     * Initializes the trigger by enabling message retrieval of the connector.
     *
     * @param jobExecution The current {@link JobExecution}.
     */
    @Override
    public void initialize(JobExecution jobExecution) {
        super.initialize(jobExecution);
        rabbitMqConnector.enableMessageRetrieval(queueName);
    }

    /**
     * Called if processing a data item finished.
     *
     * @param dataItem The data item that was processed.
     */
    @Override
    public void processingFinished(Map<String, Object> dataItem) {
        rabbitMqConnector.processingFinished(dataItem);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventType getSupportedEventType() {
        return EventType.MESSAGE;
    }

}
