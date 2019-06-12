package com.arassec.igor.core.model.provider;

import com.arassec.igor.core.model.job.execution.JobExecution;

import java.util.Map;

/**
 * Provides data for a job to process.
 */
public interface Provider {

    /**
     * Initializes the provider.
     *
     * @param jobId        The job's ID.
     * @param taskId       The task's ID.
     * @param jobExecution The job execution log.
     */
    void initialize(Long jobId, String taskId, JobExecution jobExecution);

    /**
     * Returns {@code true}, if there is further data to process.
     *
     * @return {@code true}, if there is data to process, {@code false} otherwise.
     */
    boolean hasNext();

    /**
     * Returns the next data-piece to process.
     *
     * @return A JSON-Object that contains the data to process.
     */
    Map<String, Object> next();

}
