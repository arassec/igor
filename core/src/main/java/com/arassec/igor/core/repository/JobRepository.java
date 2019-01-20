package com.arassec.igor.core.repository;

import com.arassec.igor.core.model.job.Job;

import java.util.List;

/**
 * Repository for {@link Job}s.
 */
public interface JobRepository {

    /**
     * Saves a new job or updates an existing one.
     *
     * @param job The job to save.
     * @return The updated job.
     */
    Job upsert(Job job);

    /**
     * Returns all jobs.
     *
     * @return The list of all available jobs.
     */
    List<Job> findAll();

    /**
     * Finds a job by its ID.
     *
     * @param id The job's ID.
     * @return The {@link Job}.
     */
    Job findById(Long id);

    /**
     * Deletes a job by its ID.
     *
     * @param id The ID of the job to delete.
     */
    void deleteById(Long id);

}
