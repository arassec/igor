package com.arassec.igor.module.message.connector;

import com.arassec.igor.core.model.connector.BaseConnector;

/**
 * Base class for message based connector implementations.
 */
public abstract class BaseMessageConnector extends BaseConnector implements MessageConnector {

    /**
     * Creates a new component instance.
     *
     * @param typeId The type ID.
     */
    protected BaseMessageConnector(String typeId) {
        super("message-connectors", typeId);
    }

}
