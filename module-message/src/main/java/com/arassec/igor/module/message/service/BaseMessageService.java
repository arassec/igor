package com.arassec.igor.module.message.service;

import com.arassec.igor.core.model.IgorCategory;
import com.arassec.igor.core.model.service.BaseService;

/**
 * Base class for message based service implementations.
 */
@IgorCategory("Message")
public abstract class BaseMessageService extends BaseService {

    /**
     * Sends the supplied message.
     *
     * @param message The message to send.
     */
    public abstract void sendMessage(Message message);

}
