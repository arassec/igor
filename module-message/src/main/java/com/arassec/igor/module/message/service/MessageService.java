package com.arassec.igor.module.message.service;

import com.arassec.igor.core.model.IgorServiceCategory;

/**
 * Base interface for all message related services.
 */
@IgorServiceCategory(label = "Message")
public interface MessageService {

    /**
     * Sends the supplied message.
     *
     * @param message The message to send.
     */
    void sendMessage(Message message);

}
