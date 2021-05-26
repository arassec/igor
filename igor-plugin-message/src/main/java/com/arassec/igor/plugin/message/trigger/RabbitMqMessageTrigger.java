package com.arassec.igor.plugin.message.trigger;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.trigger.BaseEventTrigger;
import com.arassec.igor.core.model.trigger.EventType;
import com.arassec.igor.plugin.core.CorePluginCategory;
import com.arassec.igor.plugin.message.MessagePluginType;
import com.arassec.igor.plugin.message.connector.RabbitMqMessageConnector;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Trigger for incoming messages that should be processed by a job.
 */
@Getter
@Setter
@IgorComponent
public class RabbitMqMessageTrigger extends BaseEventTrigger {

    /**
     * The connector that is used to receive messages.
     */
    @NotNull
    @IgorParam
    private RabbitMqMessageConnector rabbitMqConnector;

    /**
     * The RabbitMQ queue to retrieve messages from.
     */
    @NotEmpty
    @IgorParam
    private String queueName;

    /**
     * Creates a new component instance.
     */
    protected RabbitMqMessageTrigger() {
        super(CorePluginCategory.MESSAGE.getId(), MessagePluginType.RABBITMQ_MESSAGE_TRIGGER.getId());
        rabbitMqConnector = new RabbitMqMessageConnector(null);
    }

    /**
     * Initializes the trigger by registering it as message processor.
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
