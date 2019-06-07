package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.application.converter.JsonServiceConverter;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.persistence.dao.JobDao;
import com.arassec.igor.persistence.dao.JobServiceReferenceDao;
import com.arassec.igor.persistence.dao.ServiceDao;
import com.arassec.igor.persistence.entity.JobServiceReferenceEntity;
import com.arassec.igor.persistence.entity.ServiceEntity;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            serviceEntity = serviceDao.findById(service.getId())
                    .orElseThrow(() -> new IllegalStateException("No service with " + "ID " + service.getId() + " available!"));
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
            Service service = jsonServiceConverter.convert(new JSONObject(serviceEntityOptional.get().getContent()), id, true);
            service.setId(id);
            return service;
        }
        return null;
    }

    /**
     * Finds a {@link Service} by its name.
     *
     * @param name The service's name.
     * @return The service.
     */
    @Override
    public Service findByName(String name) {
        ServiceEntity serviceEntity = serviceDao.findByName(name);
        if (serviceEntity != null) {
            Service service = jsonServiceConverter
                    .convert(new JSONObject(serviceEntity.getContent()), serviceEntity.getId(), true);
            service.setId(serviceEntity.getId());
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
            Service service = jsonServiceConverter
                    .convert(new JSONObject(serviceEntity.getContent()), serviceEntity.getId(), true);
            service.setId(serviceEntity.getId());
            if (service != null) {
                result.add(service);
            }
        }
        return result;
    }

    /**
     * Finds all services for the requested page that match the optional name filter.
     *
     * @param pageNumber The page number to load.
     * @param pageSize   The page size.
     * @param nameFilter An optional filter for the service's name.
     * @return The page of services.
     */
    @Override
    public ModelPage<Service> findPage(int pageNumber, int pageSize, String nameFilter) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("name"));

        Page<ServiceEntity> page;
        if (nameFilter != null && !nameFilter.isEmpty()) {
            page = serviceDao.findByNameContainingIgnoreCase(nameFilter, pageable);
        } else {
            page = serviceDao.findAll(pageable);
        }

        if (page != null && page.hasContent()) {
            ModelPage<Service> result = new ModelPage<>(page.getNumber(), page.getSize(), page.getTotalPages(), null);
            result.setItems(page.getContent().stream().map(serviceEntity -> jsonServiceConverter
                    .convert(new JSONObject(serviceEntity.getContent()), serviceEntity.getId(), true))
                    .collect(Collectors.toList()));
            return result;
        }

        return new ModelPage<>(pageNumber, pageSize, 0, List.of());
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
    public ModelPage<Pair<Long, String>> findReferencingJobs(Long id, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<JobServiceReferenceEntity> serviceReferences = jobServiceReferenceDao.findByServiceId(id, pageable);
        if (serviceReferences != null && serviceReferences.hasContent()) {
            ModelPage<Pair<Long, String>> result = new ModelPage<>(pageNumber, pageSize, serviceReferences.getTotalPages(), null);
            result.setItems(serviceReferences.get()
                    .map(reference -> new Pair<>(reference.getJobServiceReferenceIdentity().getJobId(),
                            jobDao.findNameById(reference.getJobServiceReferenceIdentity().getJobId())))
                    .collect(Collectors.toList()));
            return result;
        }
        return new ModelPage<>(pageNumber, pageSize, 0, List.of());
    }

}
