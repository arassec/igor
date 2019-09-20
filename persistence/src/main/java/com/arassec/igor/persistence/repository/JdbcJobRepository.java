package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.repository.JobRepository;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.persistence.dao.JobDao;
import com.arassec.igor.persistence.dao.JobServiceReferenceDao;
import com.arassec.igor.persistence.dao.ServiceDao;
import com.arassec.igor.persistence.entity.JobEntity;
import com.arassec.igor.persistence.entity.JobServiceReferenceEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link JobRepository} implementation that uses JDBC to persist {@link Job}s.
 */
@Component
@Transactional
@RequiredArgsConstructor
public class JdbcJobRepository implements JobRepository {

    /**
     * DAO for job entities.
     */
    private final JobDao jobDao;

    /**
     * DAO for service entities.
     */
    private final ServiceDao serviceDao;

    /**
     * DAO for job-service-references.
     */
    private final JobServiceReferenceDao jobServiceReferenceDao;

    /**
     * ObjectMapper for JSON conversion.
     */
    private final ObjectMapper persistenceJobMapper;

    /**
     * Persists jobs using JDBC. Either creates a new entry in the database or updates an existing one.
     *
     * @param job The job to persist.
     */
    @Override
    public Job upsert(Job job) {

        // Process the job itself:
        JobEntity jobEntity;
        if (job.getId() == null) {
            jobEntity = new JobEntity();
        } else {
            jobEntity = jobDao.findById(job.getId()).orElseThrow(
                    () -> new IllegalStateException("No job with ID " + job.getId() + " available!"));
        }
        jobEntity.setName(job.getName());
        try {
            jobEntity.setContent(persistenceJobMapper.writeValueAsString(job));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not upsert job!", e);
        }
        JobEntity savedJob = jobDao.save(jobEntity);
        job.setId(savedJob.getId());

        // Now update the service references:
        jobServiceReferenceDao.deleteByJobId(savedJob.getId());
        List<Long> referencedServices = new LinkedList<>();
        job.getTasks().forEach(task -> {
            referencedServices.addAll(getServiceIds(task.getProvider()));
            task.getActions().forEach(action -> referencedServices.addAll(getServiceIds(action)));
        });
        referencedServices.forEach(serviceId -> jobServiceReferenceDao.save(new JobServiceReferenceEntity(savedJob.getId(),
                serviceId)));

        return job;
    }

    /**
     * Finds a job by its ID.
     *
     * @param id The job's ID.
     *
     * @return The {@link Job}.
     */
    @Override
    public Job findById(Long id) {
        Optional<JobEntity> jobEntityOptional = jobDao.findById(id);
        if (jobEntityOptional.isPresent()) {
            Job job;
            try {
                job = persistenceJobMapper.readValue(jobEntityOptional.get().getContent(), Job.class);
                job.setId(id);
            } catch (IOException e) {
                throw new IllegalStateException("Could not read job!", e);
            }
            job.setId(id);
            return job;
        }
        return null;
    }

    /**
     * Finds a job by its name.
     *
     * @param name The job's name.
     *
     * @return The {@link Job}.
     */
    @Override
    public Job findByName(String name) {
        JobEntity entity = jobDao.findByName(name);
        if (entity != null) {
            Job job;
            try {
                job = persistenceJobMapper.readValue(entity.getContent(), Job.class);
                job.setId(entity.getId());
            } catch (IOException e) {
                throw new IllegalStateException("Could not read job!", e);
            }
            job.setId(entity.getId());
            return job;
        }
        return null;
    }

    /**
     * Finds all jobs in the database.
     *
     * @return List of all {@link Job}s.
     */
    @Override
    public List<Job> findAll() {
        List<Job> result = new LinkedList<>();
        for (JobEntity jobEntity : jobDao.findAll()) {
            Job job;
            try {
                job = persistenceJobMapper.readValue(jobEntity.getContent(), Job.class);
                job.setId(jobEntity.getId());
            } catch (IOException e) {
                throw new IllegalStateException("Could not read job!", e);
            }
            job.setId(jobEntity.getId());
            result.add(job);
        }
        return result;
    }

    /**
     * Finds all jobs for the requested page that match the optional name filter.
     *
     * @param pageNumber The page number to load.
     * @param pageSize   The page size.
     * @param nameFilter An optional filter for the job's name.
     *
     * @return The page of jobs.
     */
    @Override
    public ModelPage<Job> findPage(int pageNumber, int pageSize, String nameFilter) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("name"));

        Page<JobEntity> page;
        if (nameFilter != null && !nameFilter.isEmpty()) {
            page = jobDao.findByNameContainingIgnoreCase(nameFilter, pageable);
        } else {
            page = jobDao.findAll(pageable);
        }

        if (page != null && page.hasContent()) {
            ModelPage<Job> result = new ModelPage<>(page.getNumber(), page.getSize(), page.getTotalPages(), null);
            result.setItems(page.getContent().stream().map(jobEntity -> {
                try {
                    Job job = persistenceJobMapper.readValue(jobEntity.getContent(), Job.class);
                    job.setId(jobEntity.getId());
                    return job;
                } catch (IOException e) {
                    throw new IllegalStateException("Could not read job!", e);
                }
            }).collect(Collectors.toList()));
            return result;
        }

        return new ModelPage<>(pageNumber, pageSize, 0, List.of());
    }

    /**
     * Deletes a job by its ID.
     *
     * @param id The ID of the job to delete.
     */
    @Override
    public void deleteById(Long id) {
        jobDao.deleteById(id);
        jobServiceReferenceDao.deleteByJobId(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Pair<Long, String>> findReferencedServices(Long id) {
        Set<Pair<Long, String>> result = new HashSet<>();

        List<JobServiceReferenceEntity> serviceReferences = jobServiceReferenceDao.findByJobId(id);
        if (serviceReferences != null) {
            serviceReferences.forEach(serviceReference -> {
                Long serviceId = serviceReference.getJobServiceReferenceIdentity().getServiceId();
                String serviceName = serviceDao.findNameById(serviceId);
                result.add(new Pair<>(serviceId, serviceName));
            });
        }

        return result;
    }

    /**
     * Extracts service IDs from the parameters of the supplied class.
     *
     * @param instance The model instance.
     * @return List of referenced service IDs.
     */
    private <T> List<Long> getServiceIds(T instance) {
        List<Long> result = new LinkedList<>();

        if (instance == null) {
            return result;
        }

        ReflectionUtils.doWithFields(instance.getClass(), field -> {
            if (field.isAnnotationPresent(IgorParam.class)) {
                boolean isService = Service.class.isAssignableFrom(field.getType());
                try {
                    boolean accessibility = field.canAccess(instance);
                    field.setAccessible(true);
                    Object value = field.get(instance);
                    field.setAccessible(accessibility);
                    if (isService && value != null) {
                        result.add(((Service) value).getId());
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Could not read service ID!", e);
                }
            }
        });

        return result;
    }

}
