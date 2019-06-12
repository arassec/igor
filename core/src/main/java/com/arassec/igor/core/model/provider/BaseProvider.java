package com.arassec.igor.core.model.provider;

import com.arassec.igor.core.model.job.execution.JobExecution;
import lombok.Getter;

/**
 * Base provider that implements common functionality of a provider.
 */
@Getter
public abstract class BaseProvider implements Provider {

    /**
     * The job's ID.
     */
    private Long jobId;

    /**
     * The task's ID.
     */
    private String taskId;

    /**
     * The job execution.
     */
    private JobExecution jobExecution;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(Long jobId, String taskId, JobExecution jobExecution) {
        this.jobId = jobId;
        this.taskId = taskId;
        this.jobExecution = jobExecution;
    }

}
