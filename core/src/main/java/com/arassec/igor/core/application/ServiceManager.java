package com.arassec.igor.core.application;

import com.arassec.igor.core.application.factory.ServiceFactory;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.ModelPageHelper;
import com.arassec.igor.core.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages {@link Service}s. Entry point from outside the core package to create and maintain services.
 */
@Component
public class ServiceManager extends ModelManager<Service> {

    /**
     * Repository for services.
     */
    private final ServiceRepository serviceRepository;

    /**
     * Creates a new instance.
     *
     * @param serviceFactory    The factory to create {@link Service}s.
     * @param serviceRepository The repository to load services from and store services into.
     */
    public ServiceManager(ServiceFactory serviceFactory, ServiceRepository serviceRepository) {
        super(serviceFactory);
        this.serviceRepository = serviceRepository;
    }

    /**
     * Saves the provided service.
     *
     * @param service The service to save.
     *
     * @return The saved service.
     */
    public Service save(Service service) {
        return serviceRepository.upsert(service);
    }

    /**
     * Loads the service with the given ID.
     *
     * @param id The service's ID.
     *
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
     *
     * @return The page with services matching the criteria.
     */
    public ModelPage<Service> loadPage(int pageNumber, int pageSize, String nameFilter) {
        return serviceRepository.findPage(pageNumber, pageSize, nameFilter);
    }

    /**
     * Loads a service by its name.
     *
     * @param name The service's name.
     *
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
     *
     * @return List of services in the category.
     */
    public ModelPage<Service> loadAllOfCategory(String category, int pageNumber, int pageSize) {
        if (category == null) {
            return new ModelPage<>(0, 0, 0, List.of());
        }
        List<Service> services = serviceRepository.findAll();
        List<Service> allOfType = services.stream()
                .filter(service -> category.equals(getModelFactory().getCategory(service).getKey()))
                .sorted(Comparator.comparing(Service::getName)).collect(Collectors.toList());
        return ModelPageHelper.getModelPage(allOfType, pageNumber, pageSize);
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
     *
     * @return Set of jobs referencing this service.
     */
    public ModelPage<Pair<Long, String>> getReferencingJobs(Long id, int pageNumber, int pageSize) {
        return serviceRepository.findReferencingJobs(id, pageNumber, pageSize);
    }

}
