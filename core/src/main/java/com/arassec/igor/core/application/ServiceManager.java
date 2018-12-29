package com.arassec.igor.core.application;

import com.arassec.igor.core.application.factory.ServiceFactory;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages {@link Service}s. Entry point from outside the core package to create and maintain services.
 */
@Component
public class ServiceManager {

    /**
     * Repository for services.
     */
    @Autowired
    private ServiceRepository serviceRepository;

    /**
     * Factory for services.
     */
    @Autowired
    private ServiceFactory serviceFactory;

    /**
     * Returns all available types of services.
     *
     * @return The service types.
     */
    public Set<String> getTypes() {
        return serviceFactory.getTypes();
    }

    /**
     * Saves the provided service.
     *
     * @param service The service to save.
     */
    public void save(Service service) {
        serviceRepository.upsert(service);
    }

    /**
     * Loads the service with the given ID.
     *
     * @param id The service's ID.
     * @return The {@link Service} with the given ID or {@code null} if none exists.
     */
    public Service load(Long id) {
        return serviceRepository.findById(id);
    }

    /**
     * Loads all available services.
     *
     * @return List of all services.
     */
    public List<Service> loadAll() {
        return serviceRepository.findAll();
    }

    /**
     * Creates a new service instance with the given parameters applied to it.
     *
     * @param type       The type of service to create.
     * @param parameters The parameters to apply to the service instance.
     * @return A newly created {@link Service} instance with the provided configuration.
     */
    public Service createService(String type, Map<String, Object> parameters) {
        return serviceFactory.createInstance(type, parameters, false);
    }

    /**
     * Deletes the service with the given ID.
     *
     * @param id The service's ID.
     */
    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }

}
