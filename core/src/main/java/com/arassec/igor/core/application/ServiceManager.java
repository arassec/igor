package com.arassec.igor.core.application;

import com.arassec.igor.core.application.factory.ServiceFactory;
import com.arassec.igor.core.application.factory.util.KeyLabelStore;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.ModelPageHelper;
import com.arassec.igor.core.util.Pair;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages {@link Service}s. Entry point from outside the core package to create and maintain services.
 */
@Component
@RequiredArgsConstructor
public class ServiceManager {

    /**
     * Repository for services.
     */
    private final ServiceRepository serviceRepository;

    /**
     * Factory for services.
     */
    private final ServiceFactory serviceFactory;

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
        List<Service> allOfType = services.stream()
                .filter(service -> category.equals(serviceFactory.getCategory(service).getKey()))
                .sorted(Comparator.comparing(Service::getName)).collect(Collectors.toList());
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
