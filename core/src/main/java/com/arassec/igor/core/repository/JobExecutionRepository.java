package com.arassec.igor.core.repository;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.util.ModelPage;

import java.util.List;

/**
 * Repository for {@link JobExecution}s.
 */
public interface JobExecutionRepository {

    /**
     * Saves a new job-execution or updates an existing one.
     *
     * @param jobExecution The job-execution to save.
     * @return The updated job-execution.
     */
    JobExecution upsert(JobExecution jobExecution);

    /**
     * Finds the job-execution with the given ID.
     *
     * @param id The job-execution's ID.
     * @return The {@link JobExecution} or {@code null}, if none exists with the given ID.
     */
    JobExecution findById(Long id);

    /**
     * Returns all executions of a certain job.
     *
     * @param jobId      The job's ID.
     * @param pageNumber The page number.
     * @param pageSize   The size of the page.
     * @return List of executions of this job.
     */
    ModelPage<JobExecution> findAllOfJob(String jobId, int pageNumber, int pageSize);

    /**
     * Returns all job-executions of a certain job in the specified state.
     *
     * @param jobId The job's ID.
     * @param state The state the job-execution should be in.
     * @return List of executions.
     */
    List<JobExecution> findAllOfJobInState(String jobId, JobExecutionState state);

    /**
     * Finds all job-executions in the specified state.
     *
     * @param state      The state of the job-exeuctions.
     * @param pageNumber The page number.
     * @param pageSize   The size of the page.
     * @return List of {@link JobExecution}s.
     */
    ModelPage<JobExecution> findInState(JobExecutionState state, int pageNumber, int pageSize);

    /**
     * Deletes old job-executions of the specified job and keeps only 'numToKeep' of the most recent executions.
     *
     * @param jobId     The job's ID.
     * @param numToKeep The number of job-executions to keep.
     */
    void cleanup(String jobId, int numToKeep);

    /**
     * Deletes all job-executions of the specified job.
     *
     * @param jobId The job's ID.
     */
    void deleteByJobId(String jobId);

    /**
     * Sets a new state to a job execution with the given ID.
     *
     * @param id       The job execution's ID.
     * @param newState The new state to set.
     */
    void updateJobExecutionState(Long id, JobExecutionState newState);

    /**
     * Sets the new state to all job executions of the given job in the supplied state.
     *
     * @param jobId    The job's ID.
     * @param oldState The old state to change.
     * @param newState The new state to set.
     */
    void updateAllJobExecutionsOfJob(String jobId, JobExecutionState oldState, JobExecutionState newState);

}
