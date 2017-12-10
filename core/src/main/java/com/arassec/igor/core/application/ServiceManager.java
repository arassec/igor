package com.arassec.igor.core.application;

import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Manages service handling.
 */
@Component
public class ServiceManager {

    @Autowired
    private ServiceRepository serviceRepository;

    public void save(Service service) {
        serviceRepository.upsert(service);
    }

    public List<Service> loadAll() {
        return serviceRepository.findAll();
    }

}
