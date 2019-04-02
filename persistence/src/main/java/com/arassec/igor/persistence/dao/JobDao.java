package com.arassec.igor.persistence.dao;

import com.arassec.igor.persistence.entity.JobEntity;
import com.arassec.igor.persistence.entity.ServiceEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Defines access to {@link com.arassec.igor.core.model.job.Job}s in the database.
 */
@Repository
public interface JobDao extends CrudRepository<JobEntity, Long> {

    /**
     * Finds a job entity by its name.
     *
     * @param name The job's name.
     * @return The job.
     */
    JobEntity findByName(String name);

    /**
     * Returns a job's name by its ID.
     *
     * @param id The job's ID.
     * @return The job's name.
     */
    @Query(value = "SELECT name FROM igor.job WHERE id = :id", nativeQuery = true)
    String findNameById(@Param("id") Long id);

}
