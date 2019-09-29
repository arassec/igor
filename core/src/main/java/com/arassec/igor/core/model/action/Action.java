package com.arassec.igor.core.model.action;

import com.arassec.igor.core.model.job.execution.JobExecution;

import java.util.List;
import java.util.Map;

/**
 * Defines an action, used by jobs. Actions take a single piece of data and process it.
 */
public interface Action {

    /**
     * Initializes the action before data processing.
     */
    void initialize();

    /**
     * Executes the action.
     *
     * @param data         The data the action will work with.
     * @param jobExecution The job's execution log.
     *
     * @return {@code true}, if the data should further be processed, {@code false} otherwise.
     */
    List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution);

    /**
     * Finalizes the action after all input data from the provider has been processed.
     *
     * @return List of final data that should be processed by later actions.
     */
    List<Map<String, Object>> complete();

    /**
     * Shuts the action down after all data has been processed. This method is called at the end of the job execution.
     *
     * @param jobId  The job's ID.
     * @param taskId The task's ID.
     */
    void shutdown(Long jobId, String taskId);

    /**
     * Returns the number of threads this action should be executed with.
     *
     * @return The number of threads.
     */
    int getNumThreads();

    /**
     * Returns whether the action is active or not.
     *
     * @return {@code true} if the action is active and should be used, {@code false} if the action should be skipped during job
     * runs.
     */
    boolean isActive();

    /**
     * Activates or deactivates the action.
     *
     * @param active Set to {@code true} to activate the action, {@code false} otherwise.
     */
    void setActive(boolean active);

}
