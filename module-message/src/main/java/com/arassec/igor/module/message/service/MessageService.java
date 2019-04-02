package com.arassec.igor.module.message.service;

import com.arassec.igor.core.model.IgorServiceCategory;
import com.arassec.igor.core.model.service.Service;

/**
 * Base interface for all message related services.
 */
@IgorServiceCategory(label = "Message")
public interface MessageService extends Service {

    /**
     * Sends the supplied message.
     *
     * @param message The message to send.
     */
    void sendMessage(Message message);

}
