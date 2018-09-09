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
 * Manages service handling.
 */
@Component
public class ServiceManager {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceFactory serviceFactory;

    public Set<String> getTypes() {
        return serviceFactory.getTypes();
    }

    public void save(Service service) {
        serviceRepository.upsert(service);
    }

    public Service load(Long id) {
        return serviceRepository.findById(id);
    }

    public List<Service> loadAll() {
        return serviceRepository.findAll();
    }

    public Service createService(String type, Map<String, Object> parameters) {
        return serviceFactory.createInstance(type, parameters, false);
    }

    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }

}
