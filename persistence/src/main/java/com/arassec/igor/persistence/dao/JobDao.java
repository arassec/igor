package com.arassec.igor.persistence.dao;

import com.arassec.igor.persistence.entity.JobEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Defines access to the jobs in the database.
 * <p>
 * Created by Andreas Sensen on 14.04.2017.
 */
@Repository
public interface JobDao extends CrudRepository<JobEntity, Long> {

}
