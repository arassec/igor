package com.arassec.igor.core.model;

import com.arassec.igor.core.model.job.execution.JobExecution;

import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

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
     * Contains the names of properties which should not be editable.
     */
    private final Set<String> unEditableProperties = new HashSet<>();

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
    public BaseIgorComponent(String categoryId, String typeId) {
        this.categoryId = categoryId;
        this.typeId = typeId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(String jobId, JobExecution jobExecution) {
        // nothing to do here...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown(String jobId, JobExecution jobExecution) {
        // nothing to do here...
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
    public Set<String> getUnEditableProperties() {
        return unEditableProperties;
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
