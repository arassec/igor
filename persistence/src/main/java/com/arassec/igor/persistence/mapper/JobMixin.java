package com.arassec.igor.persistence.mapper;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Jackson mix-in to ignore properties that shouldn't be serialized.
 */
public interface JobMixin {

    /**
     * The job-execution is completely hold in memory during the execution. It should not be persisted in the database.
     */
    @JsonIgnore
    JobExecution getCurrentJobExecution();

    /**
     * If the job is running or not is determined at runtime and should not be persisted in the database.
     */
    @JsonIgnore
    boolean isRunning();

}
