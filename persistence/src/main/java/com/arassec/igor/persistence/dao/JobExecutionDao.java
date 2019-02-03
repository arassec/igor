package com.arassec.igor.persistence.dao;

import com.arassec.igor.persistence.entity.JobExecutionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Defines access to {@link com.arassec.igor.core.model.job.execution.JobExecution}s in the database.
 */
@Repository
public interface JobExecutionDao extends CrudRepository<JobExecutionEntity, Long> {

    /**
     * Returns all persisted job-executions of the job with the provided ID.
     *
     * @param jobId The job's ID.
     * @return List of job-executions of that job.
     */
    List<JobExecutionEntity> findByJobId(Long jobId);

}
