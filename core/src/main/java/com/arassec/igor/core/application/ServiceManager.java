package com.arassec.igor.core.application;

import com.arassec.igor.core.application.factory.ServiceFactory;
import com.arassec.igor.core.application.schema.ParameterDefinition;
import com.arassec.igor.core.application.schema.ServiceCategory;
import com.arassec.igor.core.application.schema.ServiceType;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Manages service handling.
 */
@Component
public class ServiceManager {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceFactory serviceFactory;

    public List<ServiceCategory> loadCategories() {
        List<ServiceCategory> categories = new LinkedList<>(serviceFactory.getServiceCategories());
        Collections.sort(categories, Comparator.comparing(ServiceCategory::getType));
        return categories;
    }

    public List<ServiceType> loadTypesOfCategory(String category) {
        List<ServiceType> types = new LinkedList<>(serviceFactory.getTypesByCategory(category));
        Collections.sort(types, Comparator.comparing(ServiceType::getLabel));
        return types;
    }

    public List<ParameterDefinition> loadParametersOfType(String type) {
        return serviceFactory.getParameterDefinitions(serviceFactory.createInstance(type));
    }

    public Map<String, Object> loadParameters(Service service) {
        return serviceFactory.getParameters(service, true);
    }

    public void save(Service service) {
        serviceRepository.upsert(service);
    }

    public Service load(String id) {
        return serviceRepository.findById(id);
    }

    public List<Service> loadAll() {
        return serviceRepository.findAll();
    }

    public Service createService(String type, Map<String, Object> parameters) {
        return serviceFactory.createInstance(type, parameters, false);
    }

    public void deleteService(String id) {
        serviceRepository.deleteById(id);
    }

    public ServiceCategory getCategory(Service service) {
        return serviceFactory.getCategory(service);
    }

    public ServiceType getType(Service service) {
        return serviceFactory.getType(service);
    }
}
