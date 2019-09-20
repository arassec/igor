package com.arassec.igor.core.model.provider;

import com.arassec.igor.core.model.IgorParam;
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
     * Limits the results in a simulated job run.
     */
    @IgorParam
    private int simulationLimit = 25;

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
