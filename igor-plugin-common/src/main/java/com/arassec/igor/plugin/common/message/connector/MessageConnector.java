package com.arassec.igor.plugin.common.message.connector;

/**
 * Interface for message related connectors.
 */
public interface MessageConnector {

    /**
     * Sends the supplied message.
     *
     * @param message The message to send.
     */
    void sendMessage(Message message);

}
