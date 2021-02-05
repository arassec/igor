package com.arassec.igor.plugin.core.message.trigger;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.trigger.BaseEventTrigger;
import com.arassec.igor.core.model.trigger.EventType;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.message.connector.FallbackMessageConnector;
import com.arassec.igor.plugin.core.message.connector.MessageConnector;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Trigger for incoming messages that should be processed by a job.
 */
@Getter
@Setter
@IgorComponent
public class MessageTrigger extends BaseEventTrigger {

    /**
     * The connector that is used to receive messages.
     */
    @NotNull
    @IgorParam
    private MessageConnector messageConnector;

    /**
     * Creates a new component instance.
     */
    protected MessageTrigger() {
        super(CoreCategory.MESSAGE.getId(), "message-trigger");
        this.messageConnector = new FallbackMessageConnector();
    }

    /**
     * Initializes the trigger by registering it as message processor.
     *
     * @param jobExecution The current {@link JobExecution}.
     */
    @Override
    public void initialize(JobExecution jobExecution) {
        super.initialize(jobExecution);
        messageConnector.enableMessageRetrieval();
    }

    /**
     * Called if processing a data item finished.
     *
     * @param dataItem The data item that was processed.
     */
    @Override
    public void processingFinished(Map<String, Object> dataItem) {
        messageConnector.processingFinished(dataItem);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventType getSupportedEventType() {
        return EventType.MESSAGE;
    }

}
