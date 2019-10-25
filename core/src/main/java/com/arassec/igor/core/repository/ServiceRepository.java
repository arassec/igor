package com.arassec.igor.core.repository;

import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;

import java.util.List;

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
     * Returns a subset of all services matching the supplied criteria.
     *
     * @param pageNumber The page number to load.
     * @param pageSize   The page size.
     * @param nameFilter An optional filter for the service's name.
     * @return The page with services.
     */
    ModelPage<Service> findPage(int pageNumber, int pageSize, String nameFilter);

    /**
     * Finds a service by its ID.
     *
     * @param id The service's ID.
     * @return The {@link Service}.
     */
    Service findById(String id);

    /**
     * Finds a service by its name.
     *
     * @param name The service's name.
     * @return The {@link Service} with this name.
     */
    Service findByName(String name);

    /**
     * Deletes a service by its ID.
     *
     * @param id The ID of the service to delete.
     */
    void deleteById(String id);

    /**
     * Returns all jobs that use the given service.
     *
     * @param id         The service's ID.
     * @param pageNumber The page to load.
     * @param pageSize   The size of the page.
     * @return List of job IDs and names that are using this service.
     */
    ModelPage<Pair<String, String>> findReferencingJobs(String id, int pageNumber, int pageSize);

}
