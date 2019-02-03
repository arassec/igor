package com.arassec.igor.core.repository;

import com.arassec.igor.core.model.job.execution.JobExecution;

import java.util.List;

/**
 * Repository for {@link JobExecution}s.
 */
public interface JobExecutionRepository {

    /**
     * Saves a new job-execution or updates an existing one.
     *
     * @param jobId        The job's ID.
     * @param jobExecution The job-execution to save.
     * @return The updated job-execution.
     */
    JobExecution save(Long jobId, JobExecution jobExecution);

    /**
     * Returns all executions of a certain job.
     *
     * @param jobId The job's ID.
     * @return List of executions of this job.
     */
    List<JobExecution> findAllOfJob(Long jobId);

}
