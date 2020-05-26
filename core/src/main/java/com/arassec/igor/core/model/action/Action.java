package com.arassec.igor.core.model.action;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.job.execution.JobExecution;

import java.util.List;
import java.util.Map;

/**
 * Defines an action, used by jobs. Actions take a single piece of data and process it.
 */
public interface Action extends IgorComponent {

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
     * An optional name of the action.
     *
     * @return The action's name.
     */
    String getName();

    /**
     * Gives the action a name.
     *
     * @param name The name of the action.
     */
    void setName(String name);

    /**
     * Returns the number of threads this action should be executed with.
     *
     * @return The number of threads.
     */
    int getNumThreads();

    /**
     * Sets the number of threads this action should run with.
     */
    void setNumThreads(int numThreads);

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
