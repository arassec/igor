package com.arassec.igor.module.message.service;

import com.arassec.igor.core.model.service.BaseService;

/**
 * Base class for message based service implementations.
 */
public abstract class BaseMessageService extends BaseService implements MessageService {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCategoryId() {
        return "3ff532b5-73b5-44a6-b97f-05bd19fa219a";
    }

}
