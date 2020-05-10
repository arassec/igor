package com.arassec.igor.module.message.connector;

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
