package com.arassec.igor.module.message.service;

import com.arassec.igor.core.model.service.BaseService;

/**
 * Base class for message based service implementations.
 */
public abstract class BaseMessageService extends BaseService implements MessageService {

    /**
     * Creates a new component instance.
     *
     * @param typeId The type ID.
     */
    public BaseMessageService(String typeId) {
        super("message-connectors", typeId);
    }

}
