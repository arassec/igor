package com.arassec.igor.persistence.dao;

import com.arassec.igor.persistence.entity.JobEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Defines access to {@link com.arassec.igor.core.model.job.Job}s in the database.
 */
@Repository
public interface JobDao extends PagingAndSortingRepository<JobEntity, Long> {

    /**
     * Finds a job entity by its name.
     *
     * @param name The job's name.
     * @return The job.
     */
    JobEntity findByName(String name);

    /**
     * Finds a page of jobs that match the supplied name.
     *
     * @param name     The name part to search for.
     * @param pageable The page parameters to use.
     * @return The page of entities.
     */
    Page<JobEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Returns a job's name by its ID.
     *
     * @param id The job's ID.
     * @return The job's name.
     */
    @Query(value = "SELECT name FROM igor.job WHERE id = :id", nativeQuery = true)
    String findNameById(@Param("id") Long id);

}
