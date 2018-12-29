package com.arassec.igor.core.repository;

import com.arassec.igor.core.model.service.Service;

import java.util.List;

/**
 * Repository for {@link Service}s.
 */
public interface ServiceRepository {

    /**
     * Saves a new service or updates an existing one.
     *
     * @param service The service to save.
     */
    void upsert(Service service);

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

}
