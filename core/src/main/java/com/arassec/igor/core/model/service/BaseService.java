package com.arassec.igor.core.model.service;

import com.arassec.igor.core.model.BaseIgorComponent;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * Base class for services that provides a common set of functionality.
 */
public abstract class BaseService extends BaseIgorComponent implements Service {

    /**
     * The service's name.
     */
    @NotEmpty
    @Size(max = 250)
    private String name;

    /**
     * Creates a new component instance.
     *
     * @param categoryId The category ID.
     * @param typeId     The type ID.
     */
    public BaseService(String categoryId, String typeId) {
        super(categoryId, typeId);
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
