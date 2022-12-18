package com.arassec.igor.persistence.dao;

import com.arassec.igor.persistence.entity.PersistentValueEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Defines access to {@link PersistentValueEntity}s in the database.
 */
@Repository
public interface PersistentValueDao extends PagingAndSortingRepository<PersistentValueEntity, Long>,
    CrudRepository<PersistentValueEntity, Long> {

    /**
     * Returns a persisted value if it exists.
     *
     * @param jobId   The job's ID.
     * @param content The content of the value.
     * @return The {@link PersistentValueEntity} if it exists or {@code null} otherwise.
     */
    PersistentValueEntity findByJobIdAndContent(String jobId, String content);

    /**
     * Returns all persistent-value-IDs for the given job until the given limit.
     *
     * @param jobId The job's ID.
     * @param limit The limit.
     * @return List of IDs.
     */
    @Query(value = "SELECT id FROM persistent_value WHERE job_id = :jobId ORDER BY id DESC LIMIT :limit", nativeQuery = true)
    List<Integer> findMostRecentIds(@Param("jobId") String jobId, @Param("limit") int limit);

    /**
     * Deletes all entries of the given job that are older than the specified persistent-value.
     *
     * @param jobId       The job's ID.
     * @param executionId The ID of the oldest persistent-value to keep.
     */
    void deleteByJobIdAndIdBefore(String jobId, Long executionId);

    /**
     * Deletes all values of the given job.
     *
     * @param jobId The job's ID.
     */
    void deleteByJobId(String jobId);
}
