package com.arassec.igor.core.repository;

import com.arassec.igor.core.model.Job;

import java.util.List;

/**
 * Repository for jobs.
 * <p>
 * Created by Andreas Sensen on 15.04.2017.
 */
public interface JobRepository {

    /**
     * Saves a new job or updates an existing one.
     *
     * @param job The job to save.
     */
    void upsert(Job job);

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

    void deleteById(Long id);

}
