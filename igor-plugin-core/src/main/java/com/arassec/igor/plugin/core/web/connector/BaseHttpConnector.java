package com.arassec.igor.plugin.core.web.connector;

import com.arassec.igor.core.model.connector.BaseConnector;
import com.arassec.igor.plugin.core.CorePluginCategory;

/**
 * Base class for HTTP based connector implementations.
 */
public abstract class BaseHttpConnector extends BaseConnector implements HttpConnector {

    /**
     * Creates a new component instance.
     *
     * @param typeId The type ID.
     */
    protected BaseHttpConnector(String typeId) {
        super(CorePluginCategory.WEB.getId(), typeId);
    }

}
