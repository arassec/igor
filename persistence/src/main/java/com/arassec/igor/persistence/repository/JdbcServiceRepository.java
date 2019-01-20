package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.application.converter.JsonServiceConverter;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.persistence.dao.ServiceDao;
import com.arassec.igor.persistence.entity.ServiceEntity;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * {@link ServiceRepository} implementation that uses JDBC to persist {@link Service}s.
 */
@Component
@Transactional
public class JdbcServiceRepository implements ServiceRepository {

    /**
     * The DAO for services.
     */
    @Autowired
    private ServiceDao serviceDao;

    /**
     * The converter for services.
     */
    @Autowired
    private JsonServiceConverter jsonServiceConverter;

    /**
     * Saves a {@link Service} to the database. Either creates a new service or updates an existing one.
     *
     * @param service The service to save.
     */
    @Override
    public Service upsert(Service service) {
        ServiceEntity serviceEntity;
        if (service.getId() == null) {
            serviceEntity = new ServiceEntity();
        } else {
            serviceEntity = serviceDao.findById(service.getId()).orElseThrow(
                    () -> new IllegalStateException("No service with ID " + service.getId() + " available!"));
        }
        serviceEntity.setName(service.getName());
        serviceEntity.setContent(jsonServiceConverter.convert(service, true, false).toString());
        ServiceEntity savedEntity = serviceDao.save(serviceEntity);
        service.setId(savedEntity.getId());
        return service;
    }

    /**
     * Finds a {@link Service} by its ID.
     *
     * @param id The service's ID.
     * @return The service.
     */
    @Override
    public Service findById(Long id) {
        Optional<ServiceEntity> serviceEntityOptional = serviceDao.findById(id);
        if (serviceEntityOptional.isPresent()) {
            Service service = jsonServiceConverter.convert(new JSONObject(serviceEntityOptional.get().getContent()), true);
            service.setId(id);
            return service;
        }
        return null;
    }

    /**
     * Finds all services in the database.
     *
     * @return List of services.
     */
    @Override
    public List<Service> findAll() {
        List<Service> result = new LinkedList<>();
        for (ServiceEntity serviceEntity : serviceDao.findAll()) {
            Service service = jsonServiceConverter.convert(new JSONObject(serviceEntity.getContent()), true);
            service.setId(serviceEntity.getId());
            if (service != null) {
                result.add(service);
            }
        }
        return result;
    }

    /**
     * Deletes a service by its ID.
     *
     * @param id The ID of the service to delete.
     */
    @Override
    public void deleteById(Long id) {
        serviceDao.deleteById(id);
    }

}
