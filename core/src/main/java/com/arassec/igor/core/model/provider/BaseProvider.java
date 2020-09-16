package com.arassec.igor.core.model.provider;

import com.arassec.igor.core.model.BaseIgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Positive;

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
     * The job execution.
     */
    private JobExecution jobExecution;

    /**
     * Limits the results in a simulated job run.
     */
    @Setter
    @Positive
    @IgorParam(value = 100, advanced = true, defaultValue = "25")
    private int simulationLimit;

    /**
     * Creates a new component instance.
     *
     * @param categoryId The category ID.
     * @param typeId     The type ID.
     */
    public BaseProvider(String categoryId, String typeId) {
        super(categoryId, typeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(String jobId, JobExecution jobExecution) {
        this.jobId = jobId;
        this.jobExecution = jobExecution;
    }

}
