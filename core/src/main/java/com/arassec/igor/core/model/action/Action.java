package com.arassec.igor.core.model.action;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ProcessingFinishedCallback;

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
     * @return A list of data items that should further be processed, or {@code null}, if there is no further data to process.
     */
    List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution);

    /**
     * Finalizes the action after all data items have been processed.
     *
     * @return List of final data that should be processed by later actions.
     */
    default List<Map<String, Object>> complete() {
        return List.of();
    }

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
     * Returns the action's description.
     *
     * @return An optional description.
     */
    String getDescription();

    /**
     * Sets an description for the action.
     *
     * @param description The action's description.
     */
    void setDescription(String description);

    /**
     * Returns the number of threads this action should be executed with.
     *
     * @return The number of threads.
     */
    int getNumThreads();

    /**
     * Sets the number of threads this action should run with.
     *
     * @param numThreads The number of threads the action should run with.
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

    /**
     * Returns whether the action supports an event stream or not.
     * <p>
     * Not all actions support jobs that are triggered by continuous incoming events, e.g. sorting might only work if all data
     * items are known to the action.
     *
     * @return {@code true} if the action can be used in event-triggered jobs, {@code false} otherwise.
     */
    default boolean supportsEvents() {
        return true;
    }

    /**
     * Sets a processing finished callback to the action which is invoked after the action finishes processing a data item.
     *
     * @param processingFinishedCallback The callback to invoke after processing a data item.
     */
    void setProcessingFinishedCallback(ProcessingFinishedCallback processingFinishedCallback);

    /**
     * Returns the callback which is invoked after processing a data item finishes.
     *
     * @return The {@link ProcessingFinishedCallback} if set or {@code null} otherwise.
     */
    ProcessingFinishedCallback getProcessingFinishedCallback();

}
