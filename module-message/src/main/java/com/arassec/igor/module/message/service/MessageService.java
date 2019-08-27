package com.arassec.igor.module.message.service;

import com.arassec.igor.core.model.IgorCategory;
import com.arassec.igor.core.model.service.Service;

/**
 * Base interface for all message related services.
 */
@IgorCategory("Message")
public interface MessageService extends Service {

    /**
     * Sends the supplied message.
     *
     * @param message The message to send.
     */
    void sendMessage(Message message);

}
