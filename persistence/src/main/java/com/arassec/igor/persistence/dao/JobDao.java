package com.arassec.igor.persistence.dao;

import com.arassec.igor.persistence.entity.JobEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Defines access to {@link com.arassec.igor.core.model.job.Job}s in the database.
 */
@Repository
public interface JobDao extends CrudRepository<JobEntity, Long> {
}
