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
    ModelPage findAllOfJob(Long jobId, int pageNumber, int pageSize);

    /**
     * Returns all job-executions of a certain job in the specified state.
     *
     * @param jobId The job's ID.
     * @param state The state the job-execution should be in.
     * @return List of executions.
     */
    List<JobExecution> findAllOfJobInState(Long jobId, JobExecutionState state);

    /**
     * Finds all job-executions in the specified state.
     *
     * @param state The state of the job-exeuctions.
     * @return List of {@link JobExecution}s.
     */
    List<JobExecution> findInState(JobExecutionState state);

    /**
     * Deletes old job-executions of the specified job and keeps only 'numToKeep' of the most recent executions.
     *
     * @param jobId     The job's ID.
     * @param numToKeep The number of job-executions to keep.
     */
    void cleanup(Long jobId, int numToKeep);

    /**
     * Deletes all job-executions of the specified job.
     *
     * @param jobId The job's ID.
     */
    void deleteByJobId(Long jobId);

    /**
     * Sets the state of all job-executions to the target state if they are in the supplied 'from'-state.
     *
     * @param fromState The state an execution must be in to be affected.
     * @param toState   The target state.
     */
    void updateState(JobExecutionState fromState, JobExecutionState toState);

}
