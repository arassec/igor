package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.repository.JobExecutionRepository;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.persistence.dao.JobExecutionDao;
import com.arassec.igor.persistence.entity.JobExecutionEntity;
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
import java.util.List;
import java.util.Optional;

/**
 * {@link com.arassec.igor.core.repository.JobExecutionRepository} implementation that uses JDBC to persist {@link
 * com.arassec.igor.core.model.job.execution.JobExecution}s.
 */
@Component
@Transactional
@RequiredArgsConstructor
public class JdbcJobExecutionRepository implements JobExecutionRepository {

    /**
     * DAO for job-execution entities.
     */
    private final JobExecutionDao jobExecutionDao;

    /**
     * ObjectMapper for JSON conversion.
     */
    private final ObjectMapper persistenceJobMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public JobExecution upsert(JobExecution jobExecution) {
        if (jobExecution == null) {
            return null;
        }
        JobExecutionEntity entity;
        if (jobExecution.getId() != null) {
            Optional<JobExecutionEntity> jobExecutionEntityOptional = jobExecutionDao.findById(jobExecution.getId());
            if (jobExecutionEntityOptional.isPresent()) {
                entity = jobExecutionEntityOptional.get();
            } else {
                // This can happen if a job is deleted while running.
                return null;
            }
        } else {
            entity = new JobExecutionEntity();
            entity.setJobId(jobExecution.getJobId());
        }
        entity.setState(jobExecution.getExecutionState().name());
        try {
            entity.setContent(persistenceJobMapper.writeValueAsString(jobExecution));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not convert job execution to JSON!", e);
        }
        JobExecutionEntity persistedEntity = jobExecutionDao.save(entity);
        jobExecution.setId(persistedEntity.getId());
        return jobExecution;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JobExecution findById(Long id) {
        var jobExecutionEntity = jobExecutionDao.findById(id).orElse(null);
        if (jobExecutionEntity != null) {
            return convert(jobExecutionEntity);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelPage<JobExecution> findAllOfJob(String jobId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id").descending());
        Page<JobExecutionEntity> page = jobExecutionDao.findByJobId(jobId, pageable);
        return convertpage(page, pageNumber);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<JobExecution> findAllOfJobInState(String jobId, JobExecutionState state) {
        List<JobExecutionEntity> jobExecutionEntities = jobExecutionDao.findByJobIdAndStateOrderByIdDesc(jobId, state.name());
        if (jobExecutionEntities != null) {
            return jobExecutionEntities.stream().map(this::convert).toList();
        }
        return List.of();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countAllOfJobInState(String jobId, JobExecutionState state) {
        return jobExecutionDao.countAllOfJobInState(jobId, state.name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelPage<JobExecution> findInState(JobExecutionState state, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id").descending());
        Page<JobExecutionEntity> page = jobExecutionDao.findByState(state.name(), pageable);
        return convertpage(page, pageNumber);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup(String jobId, int numToKeep) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("id").descending());
        Page<JobExecutionEntity> page = jobExecutionDao.findByJobId(jobId, pageable);
        if (page != null && page.hasContent() && page.getTotalElements() > numToKeep) {

            var i = 0;
            for (JobExecutionEntity entity : page.getContent()) {
                if (!JobExecutionState.FAILED.name().equals(entity.getState())) {
                    i++;
                }
                if (i == numToKeep) {
                    jobExecutionDao.deleteByJobIdAndStateNotAndIdBefore(jobId, JobExecutionState.FAILED.name(), entity.getId());
                    break;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteByJobId(String jobId) {
        if (jobId != null) {
            jobExecutionDao.deleteByJobId(jobId);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void updateJobExecutionState(Long id, JobExecutionState newState) {
        if (id != null && newState != null) {
            jobExecutionDao.findById(id).ifPresent(jobExecutionEntity -> jobExecutionEntity.setState(newState.name()));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void updateAllJobExecutionsOfJob(String jobId, JobExecutionState oldState, JobExecutionState newState) {
        if (jobId != null && oldState != null && newState != null) {
            List<JobExecutionEntity> executionEntities = jobExecutionDao.findByJobIdAndStateOrderByIdDesc(jobId, oldState.name());
            if (executionEntities != null) {
                executionEntities.forEach(executionEntity -> executionEntity.setState(newState.name()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countJobsWithState(JobExecutionState state) {
        return jobExecutionDao.countDistinctJobIdByState(state.name());
    }

    /**
     * Converts a page of entities into a page of {@link JobExecution}s.
     *
     * @param page       The Spring-Data page.
     * @param pageNumber The requested page number.
     *
     * @return A {@link ModelPage} with {@link JobExecution}s.
     */
    private ModelPage<JobExecution> convertpage(Page<JobExecutionEntity> page, int pageNumber) {
        if (page != null && page.hasContent()) {
            ModelPage<JobExecution> result = new ModelPage<>(page.getNumber(), page.getSize(), page.getTotalPages(), null);
            result.setItems(page.getContent().stream().map(this::convert).toList());
            return result;
        }
        return new ModelPage<>(pageNumber, 0, 0, List.of());
    }

    /**
     * Converts a job-execution database entity into a {@link JobExecution}.
     *
     * @param entity The entity.
     *
     * @return The newly created {@link JobExecution}.
     */
    private JobExecution convert(JobExecutionEntity entity) {
        JobExecution result;
        try {
            result = persistenceJobMapper.readValue(entity.getContent(), JobExecution.class);
            result.setId(entity.getId());
            result.setExecutionState(JobExecutionState.valueOf(entity.getState()));
        } catch (IOException e) {
            throw new IllegalStateException("Could not read job execution!", e);
        }
        return result;
    }

}
