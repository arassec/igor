package com.arassec.igor.core.model.service;

/**
 * Base for services that contains misc functionality.
 * <p>
 * Created by sensen on 3/29/17.
 */
public abstract class BaseService implements Service {

    /**
     * The service's ID.
     */
    private String id;

    /**
     * The service's description.
     */
    private String description;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

}
