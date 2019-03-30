package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.application.converter.JsonServiceConverter;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.persistence.dao.JobDao;
import com.arassec.igor.persistence.dao.JobServiceReferenceDao;
import com.arassec.igor.persistence.dao.ServiceDao;
import com.arassec.igor.persistence.entity.JobServiceReferenceEntity;
import com.arassec.igor.persistence.entity.ServiceEntity;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
     * The DAO for jobs.
     */
    @Autowired
    private JobDao jobDao;

    /**
     * DAO for job-service-references.
     */
    @Autowired
    private JobServiceReferenceDao jobServiceReferenceDao;

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
        jobServiceReferenceDao.deleteByServiceId(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Pair<Long, String>> findReferencingJobs(Long id) {
        Set<Pair<Long, String>> result = new HashSet<>();

        List<JobServiceReferenceEntity> serviceReferences = jobServiceReferenceDao.findByServiceId(id);
        if (serviceReferences != null) {
            serviceReferences.forEach(serviceReference -> {
                Long jobId = serviceReference.getJobServiceReferenceIdentity().getJobId();
                String jobName = jobDao.findNameById(jobId);
                result.add(new Pair<>(jobId, jobName));
            });
        }

        return result;
    }

}
