package com.arassec.igor.core.model.service;

/**
 * Base for services that contains persistence functionality.
 * <p>
 * Created by sensen on 3/29/17.
 */
public abstract class BaseService implements Service {

    /**
     * The service's ID.
     */
    private String id;

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
