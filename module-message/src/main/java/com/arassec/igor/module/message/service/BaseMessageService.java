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
        super("3ff532b5-73b5-44a6-b97f-05bd19fa219a", typeId);
    }

}
