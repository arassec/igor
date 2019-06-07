package com.arassec.igor.persistence.dao;

import com.arassec.igor.persistence.entity.PersistentValueEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Defines access to {@link PersistentValueEntity}s in the database.
 */
@Repository
public interface PersistentValueDao extends PagingAndSortingRepository<PersistentValueEntity, Long> {

    /**
     * Returns a persisted value if it exists.
     *
     * @param jobId   The job's ID.
     * @param taskId  The task's ID.
     * @param content The content of the value.
     * @return The {@link PersistentValueEntity} if it exists or {@code null} otherwise.
     */
    PersistentValueEntity findByJobIdAndTaskIdAndContent(Long jobId, String taskId, String content);

    /**
     * Returns all persistent-value-IDs for the given job and task until the given limit.
     *
     * @param jobId  The job's ID.
     * @param taskId The task's ID.
     * @param limit  The limit.
     * @return List of IDs.
     */
    @Query(value = "SELECT id FROM igor.persistent_value WHERE job_id = :jobId AND task_id = :taskId ORDER BY id DESC LIMIT :limit",
            nativeQuery = true)
    List<Integer> findMostRecentIds(@Param("jobId") Long jobId, @Param("taskId") String taskId, @Param("limit") int limit);

    /**
     * Deletes all entries of the given job and task that are older than the specified persistent-value.
     *
     * @param jobId       The job's ID.
     * @param taskId      The task's ID.
     * @param executionId The ID of the oldest persistent-value to keep.
     */
    void deleteByJobIdAndTaskIdAndIdBefore(Long jobId, String taskId, Long executionId);

    /**
     * Deletes all values of the given job.
     *
     * @param jobId The job's ID.
     */
    void deleteByJobId(Long jobId);
}
