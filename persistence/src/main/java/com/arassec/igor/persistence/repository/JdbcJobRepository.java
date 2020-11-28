package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.repository.JobRepository;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.persistence.dao.ConnectorDao;
import com.arassec.igor.persistence.dao.JobConnectorReferenceDao;
import com.arassec.igor.persistence.dao.JobDao;
import com.arassec.igor.persistence.entity.JobConnectorReferenceEntity;
import com.arassec.igor.persistence.entity.JobConnectorReferenceView;
import com.arassec.igor.persistence.entity.JobEntity;
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
     * Error message if reading a job fails.
     */
    private static final String READ_JOB_ERROR = "Could not read job!";

    /**
     * DAO for job entities.
     */
    private final JobDao jobDao;

    /**
     * DAO for connector entities.
     */
    private final ConnectorDao connectorDao;

    /**
     * DAO for job-connector-references.
     */
    private final JobConnectorReferenceDao jobConnectorReferenceDao;

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
        JobEntity jobEntity = null;
        if (job.getId() == null) {
            job.setId(UUID.randomUUID().toString());
        } else {
            jobEntity = jobDao.findById(job.getId()).orElse(null);
        }

        if (jobEntity == null) {
            jobEntity = new JobEntity();
            jobEntity.setId(job.getId());
        }

        jobEntity.setName(job.getName());

        // Generate IDs if necessary:
        if (job.getTrigger() != null && job.getTrigger().getId() == null) {
            job.getTrigger().setId(UUID.randomUUID().toString());
        }
        job.getActions().forEach(action -> {
            if (action.getId() == null) {
                action.setId(UUID.randomUUID().toString());
            }
        });

        try {
            jobEntity.setContent(persistenceJobMapper.writeValueAsString(job));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not upsert job!", e);
        }

        jobDao.save(jobEntity);

        // Now update the connector references:
        jobConnectorReferenceDao.deleteByJobId(job.getId());
        List<String> referencedConnectors = new LinkedList<>(getConnectorIds(job.getTrigger()));
        job.getActions().forEach(action -> referencedConnectors.addAll(getConnectorIds(action)));

        referencedConnectors.forEach(connectorId -> jobConnectorReferenceDao.save(new JobConnectorReferenceEntity(job.getId(),
                connectorId)));

        return job;
    }

    /**
     * Finds a job by its ID.
     *
     * @param jobId The job's ID.
     *
     * @return The {@link Job}.
     */
    @Override
    public Job findById(String jobId) {
        Optional<JobEntity> jobEntityOptional = jobDao.findById(jobId);
        if (jobEntityOptional.isPresent()) {
            try {
                return persistenceJobMapper.readValue(jobEntityOptional.get().getContent(), Job.class);
            } catch (IOException e) {
                throw new IllegalStateException(READ_JOB_ERROR, e);
            }
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
            try {
                return persistenceJobMapper.readValue(entity.getContent(), Job.class);
            } catch (IOException e) {
                throw new IllegalStateException(READ_JOB_ERROR, e);
            }
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
            try {
                result.add(persistenceJobMapper.readValue(jobEntity.getContent(), Job.class));
            } catch (IOException e) {
                throw new IllegalStateException(READ_JOB_ERROR, e);
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
                    return persistenceJobMapper.readValue(jobEntity.getContent(), Job.class);
                } catch (IOException e) {
                    throw new IllegalStateException(READ_JOB_ERROR, e);
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
    public void deleteById(String id) {
        jobDao.deleteById(id);
        jobConnectorReferenceDao.deleteByJobId(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Pair<String, String>> findReferencedConnectors(String jobId) {
        Set<Pair<String, String>> result = new HashSet<>();

        List<JobConnectorReferenceView> connectorReferences = jobConnectorReferenceDao.findByJobId(jobId);
        if (connectorReferences != null) {
            connectorReferences.forEach(connectorReference ->
                    result.add(new Pair<>(connectorReference.getConnectorId(), connectorReference.getConnectorName())));
        }

        return result;
    }

    /**
     * Extracts connector IDs from the parameters of the supplied class.
     *
     * @param instance The model instance.
     *
     * @return List of referenced connector IDs.
     */
    private <T extends IgorComponent> List<String> getConnectorIds(T instance) {
        List<String> result = new LinkedList<>();

        if (instance == null) {
            return result;
        }

        ReflectionUtils.doWithFields(instance.getClass(), field -> {
            if (field.isAnnotationPresent(IgorParam.class)) {
                try {
                    ReflectionUtils.makeAccessible(field);
                    Object value = field.get(instance);
                    if (value instanceof Connector) {
                        result.add(((Connector) value).getId());
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Could not read connector ID!", e);
                }
            }
        });

        return result;
    }

}
