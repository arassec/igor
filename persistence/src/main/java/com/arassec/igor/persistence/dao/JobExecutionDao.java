package com.arassec.igor.persistence.dao;

import com.arassec.igor.persistence.entity.JobExecutionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Defines access to {@link com.arassec.igor.core.model.job.execution.JobExecution}s in the database.
 */
@Repository
public interface JobExecutionDao extends PagingAndSortingRepository<JobExecutionEntity, Long> {

    /**
     * Returns all persisted job-executions of the job with the provided ID.
     *
     * @param jobId    The job's ID.
     * @param pageable The page parameters to use.
     *
     * @return List of job-executions of that job.
     */
    Page<JobExecutionEntity> findByJobId(String jobId, Pageable pageable);

    /**
     * Returns all job-executions of a certain job in the given state with the newest entries first.
     *
     * @param jobId The job's ID.
     * @param state The state the job-executions should be in.
     *
     * @return A List of job-executions that match the criteria.
     */
    List<JobExecutionEntity> findByJobIdAndStateOrderByIdDesc(String jobId, String state);

    /**
     * Returns all job-executions in the specified state, oldest entries first.
     *
     * @param state    The state the job-executions should be in.
     * @param pageable The page parameters to use.
     *
     * @return A list of job-executions in the desired state or null, if none exist.
     */
    Page<JobExecutionEntity> findByState(String state, Pageable pageable);

    /**
     * Deletes all entries of the given job that are older than the specified execution.
     *
     * @param jobId       The job's ID.
     * @param state       The job execution state that should be ignored by the deletion.
     * @param executionId The ID of the oldest job-execution to keep.
     */
    void deleteByJobIdAndStateNotAndIdBefore(String jobId, String state, Long executionId);

    /**
     * Deletes all entries of the specified job.
     *
     * @param jobId The job's ID.
     */
    void deleteByJobId(String jobId);

    /**
     * Counts job executions in the given state.
     *
     * @param state The state a job execution has to be in to count.
     *
     * @return The number of executions in the given state.
     */
    @Query(value = "SELECT COUNT(DISTINCT (job_id)) FROM job_execution WHERE state = :state", nativeQuery = true)
    int countDistinctJobIdByState(String state);

}
