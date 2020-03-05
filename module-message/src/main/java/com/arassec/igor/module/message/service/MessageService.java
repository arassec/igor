package com.arassec.igor.module.message.service;

/**
 * Interface for message related services.
 */
public interface MessageService {

    /**
     * Sends the supplied message.
     *
     * @param message The message to send.
     */
    void sendMessage(Message message);

}
