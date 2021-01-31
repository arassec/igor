package com.arassec.igor.plugin.core.message.connector;

import com.arassec.igor.core.model.job.misc.ProcessingFinishedCallback;

/**
 * Interface for message related connectors.
 */
public interface MessageConnector extends ProcessingFinishedCallback {

    /**
     * Sends the supplied message.
     *
     * @param message The message to send.
     */
    void sendMessage(Message message);

    /**
     * Enables the message connector to receive messages.
     */
    default void enableMessageRetrieval() {
    }

}
