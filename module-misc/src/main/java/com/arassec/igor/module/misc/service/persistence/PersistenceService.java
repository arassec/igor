package com.arassec.igor.module.misc.service.persistence;

import com.arassec.igor.core.model.IgorServiceCategory;
import com.arassec.igor.core.model.service.Service;

import java.util.List;

/**
 * Service for persisting data.
 */
@IgorServiceCategory(label = "Persistence")
public interface PersistenceService extends Service {

    /**
     * Saves the provided value under the job's and task's ID.
     *
     * @param jobId  The job's ID.
     * @param taskId The task's ID.
     * @param value  The value to persist.
     */
    void save(Long jobId, String taskId, String value);

    /**
     * Returns whether a value is already persisted or not.
     *
     * @param jobId  The job's ID.
     * @param taskId The task's ID.
     * @param value  The value to test.
     * @return {@code true} if the value is already persisted, {@code false} otherwise.
     */
    boolean isPersisted(Long jobId, String taskId, String value);

    /**
     * Cleans up the persistence store by keeping only the provided number of values. Only the most recent values are
     * kept, older values are deleted if necessary.
     *
     * @param jobId            The job's ID.
     * @param taskId           The task's ID.
     * @param numEntriesToKeep Number of values that should be kept in the persistence store.
     */
    void cleanup(Long jobId, String taskId, int numEntriesToKeep);

}
