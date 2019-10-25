package com.arassec.igor.core.repository;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;

import java.util.List;
import java.util.Set;

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
     * Returns a subset of all jobs matching the supplied criteria.
     *
     * @param pageNumber The page number to load.
     * @param pageSize   The page size.
     * @param nameFilter An optional filter for the job's name.
     * @return The page with jobs.
     */
    ModelPage<Job> findPage(int pageNumber, int pageSize, String nameFilter);

    /**
     * Finds a job by its ID.
     *
     * @param id The job's ID.
     * @return The {@link Job}.
     */
    Job findById(String id);

    /**
     * Finds a job by its name.
     *
     * @param name The job's name.
     * @return The {@link Job}.
     */
    Job findByName(String name);

    /**
     * Deletes a job by its ID.
     *
     * @param id The ID of the job to delete.
     */
    void deleteById(String id);

    /**
     * Returns all services that are used by the given job.
     *
     * @param id The job's ID.
     * @return List of service IDs and names used by this job.
     */
    Set<Pair<String, String>> findReferencedServices(String id);

}
