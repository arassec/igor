package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.application.converter.JsonJobConverter;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.repository.JobRepository;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.persistence.dao.JobDao;
import com.arassec.igor.persistence.dao.JobServiceReferenceDao;
import com.arassec.igor.persistence.dao.ServiceDao;
import com.arassec.igor.persistence.entity.JobEntity;
import com.arassec.igor.persistence.entity.JobServiceReferenceEntity;
import com.arassec.igor.persistence.util.ServiceIdExtractor;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link JobRepository} implementation that uses JDBC to persist {@link Job}s.
 */
@Component
@Transactional
public class JdbcJobRepository implements JobRepository {

    /**
     * DAO for job entities.
     */
    @Autowired
    private JobDao jobDao;

    /**
     * DAO for service entities.
     */
    @Autowired
    private ServiceDao serviceDao;

    /**
     * DAO for job-service-references.
     */
    @Autowired
    private JobServiceReferenceDao jobServiceReferenceDao;

    /**
     * Converter for jobs.
     */
    @Autowired
    private JsonJobConverter jsonJobConverter;

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
            jobEntity =
                    jobDao.findById(job.getId()).orElseThrow(() -> new IllegalStateException("No job with ID " + job.getId() +
                            " available!"));
        }
        jobEntity.setName(job.getName());
        jobEntity.setContent(jsonJobConverter.convert(job, true, false).toString());
        JobEntity savedJob = jobDao.save(jobEntity);
        job.setId(savedJob.getId());

        // Now update the service references:
        jobServiceReferenceDao.deleteByJobId(savedJob.getId());
        List<Long> referencedServices = new LinkedList<>();
        job.getTasks().forEach(task -> {
            referencedServices.addAll(ServiceIdExtractor.getServiceIds(task.getProvider()));
            task.getActions().forEach(action -> referencedServices.addAll(ServiceIdExtractor.getServiceIds(action)));
        });
        referencedServices.forEach(serviceId -> jobServiceReferenceDao.save(new JobServiceReferenceEntity(savedJob.getId(),
                serviceId)));

        return job;
    }

    /**
     * Finds a job by its ID.
     *
     * @param id The job's ID.
     * @return The {@link Job}.
     */
    @Override
    public Job findById(Long id) {
        Optional<JobEntity> jobEntityOptional = jobDao.findById(id);
        if (jobEntityOptional.isPresent()) {
            Job job = jsonJobConverter.convert(new JSONObject(jobEntityOptional.get().getContent()), id, true);
            job.setId(id);
            return job;
        }
        return null;
    }

    /**
     * Finds a job by its name.
     *
     * @param name The job's name.
     * @return The {@link Job}.
     */
    @Override
    public Job findByName(String name) {
        JobEntity entity = jobDao.findByName(name);
        if (entity != null) {
            Job job = jsonJobConverter.convert(new JSONObject(entity.getContent()), entity.getId(), true);
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
            Job job = jsonJobConverter.convert(new JSONObject(jobEntity.getContent()), jobEntity.getId(), true);
            job.setId(jobEntity.getId());
            if (job != null) {
                result.add(job);
            }
        }
        return result;
    }

    /**
     * Finds all jobs for the requested page that match the optional name filter.
     *
     * @param pageNumber The page number to load.
     * @param pageSize   The page size.
     * @param nameFilter An optional filter for the job's name.
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
            result.setItems(page.getContent().stream().map(jobEntity -> jsonJobConverter.convert(new JSONObject(jobEntity.getContent()), jobEntity.getId(), true)).collect(Collectors.toList()));
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

}
