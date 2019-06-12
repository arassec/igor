package com.arassec.igor.core.application;

import com.arassec.igor.core.application.factory.ServiceFactory;
import com.arassec.igor.core.application.factory.util.KeyLabelStore;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.ModelPageHelper;
import com.arassec.igor.core.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

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
     * Returns all available service categories.
     *
     * @return The categories.
     */
    public Set<KeyLabelStore> getCategories() {
        return serviceFactory.getCategories();
    }

    /**
     * Returns all available types of services of the specified category.
     *
     * @return The service types of the specified category.
     */
    public Set<KeyLabelStore> getTypesOfCategory(String categoryKey) {
        if (serviceFactory.getTypesByCategory().containsKey(categoryKey)) {
            return serviceFactory.getTypesByCategory().get(categoryKey);
        }
        return new HashSet<>();
    }

    /**
     * Saves the provided service.
     *
     * @param service The service to save.
     * @return The saved service.
     */
    public Service save(Service service) {
        return serviceRepository.upsert(service);
    }

    /**
     * Loads the service with the given ID.
     *
     * @param id The service's ID.
     * @return The {@link Service} with the given ID or {@code null}, if none exists.
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
     * Loads a page of services matching the supplied criteria.
     *
     * @param pageNumber The page number.
     * @param pageSize   The page size.
     * @param nameFilter An optional name filter for the services.
     * @return The page with services matching the criteria.
     */
    public ModelPage<Service> loadPage(int pageNumber, int pageSize, String nameFilter) {
        return serviceRepository.findPage(pageNumber, pageSize, nameFilter);
    }

    /**
     * Loads a service by its name.
     *
     * @param name The service's name.
     * @return The {@link Service} with the given name or {@code null}, if none exists.
     */
    public Service loadByName(String name) {
        return serviceRepository.findByName(name);
    }

    /**
     * Loads all service of a given category.
     *
     * @param category   The category to filter services with.
     * @param pageNumber The page to load.
     * @param pageSize   The size of the page.
     * @return List of services in the category.
     */
    public ModelPage<Service> loadAllOfCategory(String category, int pageNumber, int pageSize) {
        if (category == null) {
            return new ModelPage<>(0, 0, 0, List.of());
        }
        List<Service> services = serviceRepository.findAll();
        List<Service> allOfType =
                services.stream().filter(service -> category.equals(serviceFactory.getCategory(service).getKey())).collect(Collectors.toList());

        Collections.sort(allOfType, Comparator.comparing(Service::getName));

        return ModelPageHelper.getModelPage(allOfType, pageNumber, pageSize);
    }

    /**
     * Creates a new service instance with the given parameters applied to it.
     *
     * @param type       The type of service to create.
     * @param parameters The parameters to apply to the service instance.
     * @return A newly created {@link Service} instance with the provided configuration.
     */
    public Service createService(String type, Map<String, Object> parameters) {
        return serviceFactory.createInstance(type, parameters);
    }

    /**
     * Deletes the service with the given ID.
     *
     * @param id The service's ID.
     */
    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }

    /**
     * Searches for jobs referencing the service with the given ID.
     *
     * @param id         The service's ID.
     * @param pageNumber The page to load.
     * @param pageSize   The size of the page.
     * @return Set of jobs referencing this service.
     */
    public ModelPage<Pair<Long, String>> getReferencingJobs(Long id, int pageNumber, int pageSize) {
        return serviceRepository.findReferencingJobs(id, pageNumber, pageSize);
    }

}
