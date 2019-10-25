package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.persistence.dao.JobDao;
import com.arassec.igor.persistence.dao.JobServiceReferenceDao;
import com.arassec.igor.persistence.dao.ServiceDao;
import com.arassec.igor.persistence.entity.JobServiceReferenceEntity;
import com.arassec.igor.persistence.entity.ServiceEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * {@link ServiceRepository} implementation that uses JDBC to persist {@link Service}s.
 */
@Component
@Transactional
@RequiredArgsConstructor
public class JdbcServiceRepository implements ServiceRepository {

    /**
     * The DAO for services.
     */
    private final ServiceDao serviceDao;

    /**
     * The DAO for jobs.
     */
    private final JobDao jobDao;

    /**
     * DAO for job-service-references.
     */
    private final JobServiceReferenceDao jobServiceReferenceDao;

    /**
     * ObjectMapper for JSON conversion.
     */
    private final ObjectMapper persistenceServiceMapper;

    /**
     * Saves a {@link Service} to the database. Either creates a new service or updates an existing one.
     *
     * @param service The service to save.
     */
    @Override
    public Service upsert(Service service) {
        ServiceEntity serviceEntity;

        if (service.getId() == null) {
            service.setId(UUID.randomUUID().toString());
            serviceEntity = new ServiceEntity();
            serviceEntity.setId(service.getId());
        } else {
            serviceEntity = serviceDao.findById(service.getId())
                    .orElseThrow(() -> new IllegalStateException("No service with " + "ID " + service.getId() + " available!"));
        }
        serviceEntity.setName(service.getName());

        try {
            serviceEntity.setContent(persistenceServiceMapper.writeValueAsString(service));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not save service!", e);
        }

       serviceDao.save(serviceEntity);

        return service;
    }

    /**
     * Finds a {@link Service} by its ID.
     *
     * @param id The service's ID.
     *
     * @return The service.
     */
    @Override
    public Service findById(String id) {
        Optional<ServiceEntity> serviceEntityOptional = serviceDao.findById(id);
        if (serviceEntityOptional.isPresent()) {
            try {
                ServiceEntity serviceEntity = serviceEntityOptional.get();
                return persistenceServiceMapper.readValue(serviceEntity.getContent(), Service.class);
            } catch (IOException e) {
                throw new IllegalStateException("Could not read service!", e);
            }
        }
        return null;
    }

    /**
     * Finds a {@link Service} by its name.
     *
     * @param name The service's name.
     *
     * @return The service.
     */
    @Override
    public Service findByName(String name) {
        ServiceEntity serviceEntity = serviceDao.findByName(name);
        if (serviceEntity != null) {
            try {
                return persistenceServiceMapper.readValue(serviceEntity.getContent(), Service.class);
            } catch (IOException e) {
                throw new IllegalStateException("Could not read service!", e);
            }
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
            try {
                result.add(persistenceServiceMapper.readValue(serviceEntity.getContent(), Service.class));
            } catch (IOException e) {
                throw new IllegalStateException("Could not read service!", e);
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
     *
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
            result.setItems(page.getContent().stream().map(serviceEntity -> {
                try {
                    return persistenceServiceMapper.readValue(serviceEntity.getContent(), Service.class);
                } catch (IOException e) {
                    throw new IllegalStateException("Could not read service!", e);
                }
            }).collect(Collectors.toList()));
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
    public void deleteById(String id) {
        serviceDao.deleteById(id);
        jobServiceReferenceDao.deleteByServiceId(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelPage<Pair<String, String>> findReferencingJobs(String id, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<JobServiceReferenceEntity> serviceReferences = jobServiceReferenceDao.findByServiceId(id, pageable);
        if (serviceReferences != null && serviceReferences.hasContent()) {
            ModelPage<Pair<String, String>> result = new ModelPage<>(pageNumber, pageSize, serviceReferences.getTotalPages(), null);
            result.setItems(serviceReferences.get()
                    .map(reference -> new Pair<>(reference.getJobServiceReferenceIdentity().getJobId(),
                            jobDao.findNameById(reference.getJobServiceReferenceIdentity().getJobId())))
                    .collect(Collectors.toList()));
            return result;
        }
        return new ModelPage<>(pageNumber, pageSize, 0, List.of());
    }

}
