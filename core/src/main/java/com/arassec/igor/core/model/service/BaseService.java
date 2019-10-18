package com.arassec.igor.core.model.service;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * Base class for services that provides a common set of functionality.
 */
public abstract class BaseService implements Service {

    /**
     * The service's ID.
     */
    private Long id;

    /**
     * The service's name.
     */
    @NotEmpty
    @Size(max = 250)
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
