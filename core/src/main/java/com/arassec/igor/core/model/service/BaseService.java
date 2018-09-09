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
    private Long id;

    /**
     * The service's name.
     */
    private String name;

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setId(Long id) {
        this.id = id;
    }

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
