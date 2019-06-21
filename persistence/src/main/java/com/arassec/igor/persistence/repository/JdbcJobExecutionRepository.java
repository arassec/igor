package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.application.converter.JsonJobExecutionConverter;
import com.arassec.igor.core.application.converter.JsonKeys;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.repository.JobExecutionRepository;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.persistence.dao.JobExecutionDao;
import com.arassec.igor.persistence.entity.JobExecutionEntity;
import com.github.openjson.JSONObject;
import lombok.RequiredArgsConstructor;
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
 * {@link com.arassec.igor.core.repository.JobExecutionRepository} implementation that uses JDBC to persist {@link com.arassec.igor.core.model.job.execution.JobExecution}s.
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
     * Converter for job-executions.
     */
    private final JsonJobExecutionConverter jobExecutionConverter;

    /**
     * {@inheritDoc}
     */
    @Override
    public JobExecution upsert(JobExecution jobExecution) {
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
        JSONObject jobExecutionJson = jobExecutionConverter.convert(jobExecution);
        // The next two attributes are stored in separate columns in the database:
        jobExecutionJson.remove(JsonKeys.ID);
        jobExecutionJson.remove(JsonKeys.STATE);
        entity.setContent(jobExecutionJson.toString());
        JobExecutionEntity persistedEntity = jobExecutionDao.save(entity);
        jobExecution.setId(persistedEntity.getId());
        return jobExecution;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JobExecution findById(Long id) {
        JobExecutionEntity jobExecutionEntity = jobExecutionDao.findById(id).orElse(null);
        if (jobExecutionEntity != null) {
            return convert(jobExecutionEntity);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelPage<JobExecution> findAllOfJob(Long jobId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id").descending());
        Page<JobExecutionEntity> page = jobExecutionDao.findByJobId(jobId, pageable);
        if (page != null && page.hasContent()) {
            ModelPage<JobExecution> result = new ModelPage<>(page.getNumber(), page.getSize(), page.getTotalPages(), null);
            result.setItems(page.getContent().stream().map(this::convert).collect(Collectors.toList()));
            return result;
        }
        return new ModelPage<>(pageNumber, 0, 0, List.of());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<JobExecution> findAllOfJobInState(Long jobId, JobExecutionState state) {
        List<JobExecutionEntity> jobExecutionEntities = jobExecutionDao.findByJobIdAndStateOrderByIdDesc(jobId, state.name());
        if (jobExecutionEntities != null) {
            return jobExecutionEntities.stream().map(this::convert).collect(Collectors.toList());
        }
        return new LinkedList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelPage<JobExecution> findInState(JobExecutionState state, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id").descending());
        Page<JobExecutionEntity> page = jobExecutionDao.findByState(state.name(), pageable);
        if (page != null && page.hasContent()) {
            ModelPage<JobExecution> result = new ModelPage<>(page.getNumber(), page.getSize(), page.getTotalPages(), null);
            result.setItems(page.getContent().stream().map(this::convert).collect(Collectors.toList()));
            return result;
        }
        return new ModelPage<>(pageNumber, 0, 0, List.of());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup(Long jobId, int numToKeep) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("id").descending());
        Page<JobExecutionEntity> page = jobExecutionDao.findByJobId(jobId, pageable);
        if (page != null && page.hasContent() && page.getTotalElements() > numToKeep) {

            int i = 0;
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
    public void deleteByJobId(Long jobId) {
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
    public void updateAllJobExecutionsOfJob(Long jobId, JobExecutionState oldState, JobExecutionState newState) {
        if (jobId != null && oldState != null && newState != null) {
            List<JobExecutionEntity> executionEntities = jobExecutionDao.findByJobIdAndStateOrderByIdDesc(jobId, oldState.name());
            if (executionEntities != null) {
                executionEntities.forEach(executionEntity -> executionEntity.setState(newState.name()));
            }
        }
    }

    /**
     * Converts a job-execution database entity into a {@link JobExecution}.
     *
     * @param entity The entity.
     * @return The newly created {@link JobExecution}.
     */
    private JobExecution convert(JobExecutionEntity entity) {
        JobExecution result = jobExecutionConverter.convert(new JSONObject(entity.getContent()));
        result.setId(entity.getId());
        result.setExecutionState(JobExecutionState.valueOf(entity.getState()));
        return result;
    }

}
