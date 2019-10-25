package com.arassec.igor.core.model;

import com.arassec.igor.core.model.job.execution.JobExecution;

/**
 * Base class for {@link IgorComponent}s.
 */
public abstract class BaseIgorComponent implements IgorComponent {

    /**
     * The component instance's ID.
     */
    protected String id;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(String jobId, String taskId, JobExecution jobExecution) {
        // nothing to do here...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown(String jobId, String taskId, JobExecution jobExecution) {
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

}
