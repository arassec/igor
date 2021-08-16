package com.arassec.igor.core.model;

import javax.validation.constraints.NotEmpty;

/**
 * Base class for {@link IgorComponent}s.
 */
public abstract class BaseIgorComponent implements IgorComponent {

    /**
     * The component instance's ID.
     */
    @NotEmpty
    protected String id;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setId(String id) {
        this.id = id;
    }

}
