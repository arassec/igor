package com.arassec.igor.core.model.action;

import com.arassec.igor.core.model.provider.IgorData;

import java.util.Set;

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
     * @param data The data the action will work with.
     * @return {@code true}, if the data should further be processed, {@code false} otherwise.
     */
    boolean process(IgorData data);

    /**
     * Performs a dry-run of the action for testing. I.e. no data should be modified irreversibly by this method.
     *
     * @param data The data the action will work with.
     * @return {@code true}, if the data should further be processed, {@code false} otherwise.
     */
    boolean dryRun(IgorData data);

    /**
     * Finalizes the action after all data has been processed.
     *
     * @param jobId The job's ID.
     * @param taskId The task's ID.
     */
    void complete(Long jobId, String taskId);

    /**
     * Returns the number of threads this action should be executed with.
     *
     * @return The number of threads.
     */
    int getNumThreads();

    /**
     * Returns the data keys that this action provides to further actions by adding them to the processed {@link IgorData}.
     *
     * @return The data keys this action provides.
     */
    Set<String> provides();

    /**
     * Returns the data keys that this action requires to process the supplied {@link IgorData}.
     *
     * @return The data keys this action requires.
     */
    Set<String> requires();

}
