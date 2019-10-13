package com.arassec.igor.module.message.service;

import com.arassec.igor.core.model.service.Service;

/**
 * Interface for message related services.
 */
public interface MessageService extends Service {

    /**
     * Sends the supplied message.
     *
     * @param message The message to send.
     */
    void sendMessage(Message message);

}
