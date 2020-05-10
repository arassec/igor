package com.arassec.igor.core.repository;

import com.arassec.igor.core.model.job.misc.PersistentValue;

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
     *
     * @return The persisted value.
     */
    PersistentValue upsert(String jobId, String taskId, PersistentValue value);

    /**
     * Returns whether a value is already persisted or not.
     *
     * @param jobId  The job's ID.
     * @param taskId The task's ID.
     * @param value  The value to check.
     *
     * @return {@code true} if the value is persisted, {@code false} otherwise.
     */
    boolean isPersisted(String jobId, String taskId, PersistentValue value);

    /**
     * Deletes old, persisted values.
     *
     * @param jobId            The job's ID.
     * @param taskId           The task's ID.
     * @param numEntriesToKeep Number of entries to keep.
     */
    void cleanup(String jobId, String taskId, int numEntriesToKeep);

    /**
     * Deletes all values of the given job.
     *
     * @param jobId The job's ID.
     */
    void deleteByJobId(String jobId);

}
