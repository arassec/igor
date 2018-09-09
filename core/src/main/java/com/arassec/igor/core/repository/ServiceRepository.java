package com.arassec.igor.core.repository;

import com.arassec.igor.core.model.service.Service;

import java.util.List;

/**
 * Repository for Services.
 * <p>
 * Created by Andreas Sensen on 23.04.2017.
 */
public interface ServiceRepository {

    /**
     * Saves a new service or updates an existing one.
     *
     * @param service The service to save.
     */
    void upsert(Service service);

    void deleteById(Long id);

    Service findById(Long id);

    List<Service> findAll();

}
