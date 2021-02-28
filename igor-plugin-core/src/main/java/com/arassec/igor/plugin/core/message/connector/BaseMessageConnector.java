package com.arassec.igor.plugin.core.message.connector;

import com.arassec.igor.core.model.connector.BaseConnector;
import com.arassec.igor.plugin.core.CorePluginCategory;

/**
 * Base class for message based connector implementations.
 */
public abstract class BaseMessageConnector extends BaseConnector {

    /**
     * Creates a new component instance.
     *
     * @param typeId The type ID.
     */
    protected BaseMessageConnector(String typeId) {
        super(CorePluginCategory.MESSAGE.getId(), typeId);
    }

}
