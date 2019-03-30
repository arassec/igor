package com.arassec.igor.core.repository;

import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.util.Pair;

import java.util.List;
import java.util.Set;

/**
 * Repository for {@link Service}s.
 */
public interface ServiceRepository {

    /**
     * Saves a new service or updates an existing one.
     *
     * @param service The service to save.
     * @return The saved service.
     */
    Service upsert(Service service);

    /**
     * Returns all services.
     *
     * @return The list of all available services.
     */
    List<Service> findAll();

    /**
     * Finds a service by its ID.
     *
     * @param id The service's ID.
     * @return The {@link Service}.
     */
    Service findById(Long id);

    /**
     * Deletes a service by its ID.
     *
     * @param id The ID of the service to delete.
     */
    void deleteById(Long id);

    /**
     * Returns all jobs that use the given service.
     *
     * @param id The service's ID.
     * @return List of job IDs and names that are using this service.
     */
    Set<Pair<Long, String>> findReferencingJobs(Long id);

}
