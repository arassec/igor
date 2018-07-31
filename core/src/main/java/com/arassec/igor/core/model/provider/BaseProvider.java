package com.arassec.igor.core.model.provider;

/**
 * Base provider that implements common functionality of a provider.
 */
public abstract class BaseProvider implements Provider {

    private String jobId;

    private String taskName;


    @Override
    public void initialize(String jobId, String taskName) {
        this.jobId = jobId;
        this.taskName = taskName;
    }

    public String getJobId() {
        return jobId;
    }

    public String getTaskName() {
        return taskName;
    }
}
