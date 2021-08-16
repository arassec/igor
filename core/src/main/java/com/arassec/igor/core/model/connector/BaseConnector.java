package com.arassec.igor.core.model.connector;

import com.arassec.igor.core.model.BaseIgorComponent;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * Base class for connectors that provides a common set of functionality.
 */
public abstract class BaseConnector extends BaseIgorComponent implements Connector {

    /**
     * The connector's name.
     */
    @NotEmpty
    @Size(max = 250)
    private String name;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

}
