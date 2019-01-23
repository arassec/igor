package com.arassec.igor.core.model.service.persistence;

import com.arassec.igor.core.model.IgorServiceCategory;
import com.arassec.igor.core.model.service.Service;

import java.util.List;

/**
 * Service for persisting data.
 */
@IgorServiceCategory(label = "Persistence")
public interface PersistenceService extends Service {

    /**
     * Saves the provided value under the job's ID and task's name.
     *
     * @param jobId  The job's ID.
     * @param taskId The task's ID.
     * @param value  The value to persist.
     */
    void save(String jobId, String taskId, String value);

    /**
     * Loads all persisted values for the given job and task.
     *
     * @param jobId  The job's ID.
     * @param taskId The task's ID.
     * @return All persisted values for the given parameters.
     */
    List<String> loadAll(String jobId, String taskId);

    /**
     * Returns whether a value is already persisted or not.
     *
     * @param jobId  The job's ID.
     * @param taskId The task's ID.
     * @param value  The value to test.
     * @return {@code true} if the value is already persisted, {@code false} otherwise.
     */
    boolean isPersisted(String jobId, String taskId, String value);

    /**
     * Cleans up the persistence store by keeping only the provided number of values. Only the most recent values are
     * kept, older values are deleted if necessary.
     *
     * @param jobId            The job's ID.
     * @param taskId           The task's ID.
     * @param numEntriesToKeep Number of values that should be kept in the persistence store.
     */
    void cleanup(String jobId, String taskId, int numEntriesToKeep);

}
