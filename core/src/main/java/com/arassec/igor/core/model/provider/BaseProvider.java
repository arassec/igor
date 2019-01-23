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
     * The task's ID.
     */
    private String taskId;

    /**
     * Saves the job's ID and the task's name for later use.
     *
     * @param jobId The job's ID.
     * @param taskId The task's ID.
     */
    @Override
    public void initialize(String jobId, String taskId) {
        this.jobId = jobId;
        this.taskId = taskId;
    }

}
