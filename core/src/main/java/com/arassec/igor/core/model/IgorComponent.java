package com.arassec.igor.core.model;

import com.arassec.igor.core.model.annotation.IgorSimulationSafe;
import com.arassec.igor.core.model.job.execution.JobExecution;

/**
 * Defines a component in the igor application.
 */
public interface IgorComponent {

    /**
     * Returns the ID of the instantiated component.
     *
     * @return The unique ID.
     */
    @IgorSimulationSafe
    String getId();

    /**
     * Sets the ID of the instantiated component.
     *
     * @param id The unique ID to set.
     */
    @IgorSimulationSafe
    void setId(String id);

    /**
     * Initializes the component before job executions.
     *
     * @param jobExecution Contains the state of the job execution.
     */
    default void initialize(JobExecution jobExecution) {
    }

    /**
     * Shuts the component down at the end of the job execution.
     *
     * @param jobExecution Contains the state of the job execution.
     */
    default void shutdown(JobExecution jobExecution) {
    }

}
