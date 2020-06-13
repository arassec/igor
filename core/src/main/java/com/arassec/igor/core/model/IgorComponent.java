package com.arassec.igor.core.model;

import com.arassec.igor.core.model.annotation.IgorSimulationSafe;
import com.arassec.igor.core.model.job.execution.JobExecution;

import java.util.Set;

/**
 * Defines a component in the igor application.
 */
public interface IgorComponent {

    /**
     * Returns the ID of the category of this component.
     *
     * @return The unique category ID.
     */
    @IgorSimulationSafe
    String getCategoryId();

    /**
     * Returns the ID of the type of this component.
     *
     * @return The unique type ID.
     */
    @IgorSimulationSafe
    String getTypeId();

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
     * Returns all un-editable properties.
     *
     * @return Set of names of properties which should not be edited by the user.
     */
    @IgorSimulationSafe
    Set<String> getUnEditableProperties();

    /**
     * Initializes the component before job executions.
     *
     * @param jobId        The job's ID.
     * @param taskId       The task's ID.
     * @param jobExecution Contains the state of the job execution.
     */
    void initialize(String jobId, String taskId, JobExecution jobExecution);


    /**
     * Shuts the component down at the end of the job execution.
     *
     * @param jobId        The job's ID.
     * @param taskId       The task's ID.
     * @param jobExecution Contains the state of the job execution.
     */
    void shutdown(String jobId, String taskId, JobExecution jobExecution);

}
