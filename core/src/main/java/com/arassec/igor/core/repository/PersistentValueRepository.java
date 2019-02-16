package com.arassec.igor.core.repository;

import com.arassec.igor.core.model.job.persistence.PersistentValue;

/**
 * Repository for access to {@link PersistentValue}s.
 */
public interface PersistentValueRepository {

    /**
     * Saves a value in the persistence store.
     *
     * @param jobId  The job's ID.
     * @param taskId The task's ID.
     * @param value  The value to save.
     * @return The persisted value.
     */
    PersistentValue upsert(Long jobId, String taskId, PersistentValue value);

    /**
     * Returns whether a value is already persisted or not.
     *
     * @param jobId  The job's ID.
     * @param taskId The task's ID.
     * @param value  The value to check.
     * @return {@code true} if the value is persisted, {@code false} otherwise.
     */
    boolean isPersisted(Long jobId, String taskId, PersistentValue value);

    /**
     * Deletes old, persisted values.
     *
     * @param jobId            The job's ID.
     * @param taskId           The task's ID.
     * @param numEntriesToKeep Number of entries to keep.
     */
    void cleanup(Long jobId, String taskId, int numEntriesToKeep);

}
