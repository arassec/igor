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
     * This component's category ID.
     */
    private final String categoryId;

    /**
     * This component's type ID.
     */
    private final String typeId;

    /**
     * Creates a new component instance.
     *
     * @param categoryId The category ID.
     * @param typeId     The type ID.
     */
    protected BaseIgorComponent(String categoryId, String typeId) {
        this.categoryId = categoryId;
        this.typeId = typeId;
    }

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
    public final String getCategoryId() {
        return categoryId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getTypeId() {
        return typeId;
    }

}
