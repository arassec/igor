package com.arassec.igor.core.repository;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;

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
     * Returns all executions of a certain job.
     *
     * @param jobId The job's ID.
     * @return List of executions of this job.
     */
    List<JobExecution> findAllOfJob(Long jobId);

    /**
     * Returns all job-executions of a certain job in the specified state.
     *
     * @param jobId The job's ID.
     * @param state The state the job-execution should be in.
     * @return List of executions.
     */
    List<JobExecution> findAllOfJobInState(Long jobId, JobExecutionState state);

    /**
     * Returns the execution state of a running job.
     *
     * @param jobId The job's ID.
     * @return A {@link JobExecution} if the job is currently running or {@code null} otherwise.
     */
    JobExecution findRunningOfJob(Long jobId);

    /**
     * Finds all job-executions in state 'waiting'.
     *
     * @return List of {@link JobExecution}s.
     */
    List<JobExecution> findWaiting();

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
