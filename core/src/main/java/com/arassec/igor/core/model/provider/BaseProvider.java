package com.arassec.igor.core.model.provider;

import lombok.Getter;

/**
 * Base provider that implements common functionality of a provider.
 */
@Getter
public abstract class BaseProvider implements Provider {

    /**
     * The job's ID.
     */
    private String jobId;

    /**
     * The task's name.
     */
    private String taskName;

    /**
     * Saves the job's ID and the task's name for later use.
     *
     * @param jobId The job's ID.
     * @param taskName The task's name.
     */
    @Override
    public void initialize(String jobId, String taskName) {
        this.jobId = jobId;
        this.taskName = taskName;
    }

}
