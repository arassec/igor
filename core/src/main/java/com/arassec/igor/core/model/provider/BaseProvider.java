package com.arassec.igor.core.model.provider;

import com.arassec.igor.core.model.BaseIgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import lombok.Getter;

import javax.validation.constraints.PositiveOrZero;

/**
 * Base provider that implements common functionality of a provider.
 */
@Getter
public abstract class BaseProvider extends BaseIgorComponent implements Provider {

    /**
     * The job's ID.
     */
    private String jobId;

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
    @PositiveOrZero
    @IgorParam
    private int simulationLimit = 25;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(String jobId, String taskId, JobExecution jobExecution) {
        this.jobId = jobId;
        this.taskId = taskId;
        this.jobExecution = jobExecution;
    }

}
