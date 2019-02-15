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
    List<JobExecutionEntity> findByJobIdOrderByIdDesc(Long jobId);

    /**
     * Returns all job-executions of a certain job in the given state with the newest entries first.
     *
     * @param jobId The job's ID.
     * @param state The state the job-executions should be in.
     * @return A List of job-executions that match the criteria.
     */
    List<JobExecutionEntity> findByJobIdAndStateOrderByIdDesc(Long jobId, String state);

    /**
     * Returns all job-executions in the specified state, oldest entries first.
     *
     * @param state The state the job-executions should be in.
     * @return A list of job-executions in the desired state or null, if none exist.
     */
    List<JobExecutionEntity> findByStateOrderByIdAsc(String state);

    /**
     * Deletes all entries of the given job that are older than the specified execution.
     *
     * @param jobId       The job's ID.
     * @param executionId The ID of the oldest job-execution to keep.
     */
    void deleteByJobIdAndIdBefore(Long jobId, Long executionId);

}
