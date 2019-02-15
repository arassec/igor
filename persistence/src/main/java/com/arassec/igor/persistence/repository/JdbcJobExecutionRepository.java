package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.application.converter.JsonJobExecutionConverter;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.repository.JobExecutionRepository;
import com.arassec.igor.persistence.dao.JobExecutionDao;
import com.arassec.igor.persistence.entity.JobExecutionEntity;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link com.arassec.igor.core.repository.JobExecutionRepository} implementation that uses JDBC to persist {@link com.arassec.igor.core.model.job.execution.JobExecution}s.
 */
@Component
@Transactional
public class JdbcJobExecutionRepository implements JobExecutionRepository {

    /**
     * DAO for job-execution entities.
     */
    @Autowired
    private JobExecutionDao jobExecutionDao;

    /**
     * Converter for job-executions.
     */
    @Autowired
    private JsonJobExecutionConverter jobExecutionConverter;

    /**
     * {@inheritDoc}
     */
    @Override
    public JobExecution upsert(JobExecution jobExecution) {
        JobExecutionEntity entity;
        if (jobExecution.getId() != null) {
            entity = jobExecutionDao.findById(jobExecution.getId()).orElseThrow(
                    () -> new IllegalStateException("No job-execution with ID " + jobExecution.getId() + " available!"));
        } else {
            entity = new JobExecutionEntity();
            entity.setJobId(jobExecution.getJobId());
        }
        entity.setState(jobExecution.getExecutionState().name());
        entity.setContent(jobExecutionConverter.convert(jobExecution).toString());
        JobExecutionEntity persistedEntity = jobExecutionDao.save(entity);
        jobExecution.setId(persistedEntity.getId());
        // TODO: We have to save the ID in the JSON. Not nice...
        persistedEntity.setContent(jobExecutionConverter.convert(jobExecution).toString());
        jobExecutionDao.save(entity);
        return jobExecution;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<JobExecution> findAllOfJob(Long jobId) {
        List<JobExecutionEntity> jobExecutionEntities = jobExecutionDao.findByJobIdOrderByIdDesc(jobId);
        if (jobExecutionEntities != null) {
            return jobExecutionEntities.stream().map(jobExecutionEntity ->
                    jobExecutionConverter.convert(new JSONObject(jobExecutionEntity.getContent()))).collect(Collectors.toList());
        }
        return new LinkedList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<JobExecution> findAllOfJobInState(Long jobId, JobExecutionState state) {
        List<JobExecutionEntity> jobExecutionEntities = jobExecutionDao.findByJobIdAndStateOrderByIdDesc(jobId, state.name());
        if (jobExecutionEntities != null) {
            return jobExecutionEntities.stream().map(jobExecutionEntity ->
                    jobExecutionConverter.convert(new JSONObject(jobExecutionEntity.getContent()))).collect(Collectors.toList());
        }
        return new LinkedList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JobExecution findRunningOfJob(Long jobId) {
        List<JobExecutionEntity> jobExecutionEntities = jobExecutionDao.findByJobIdAndStateOrderByIdDesc(jobId, JobExecutionState.RUNNING.name());
        if (jobExecutionEntities != null) {
            if (jobExecutionEntities.size() > 1) {
                throw new IllegalStateException("Multiple job-executions persisted for job: " + jobId);
            }
            return jobExecutionConverter.convert(new JSONObject(jobExecutionEntities.get(0).getContent()));
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<JobExecution> findWaiting() {
        List<JobExecutionEntity> waitingJobExecutions = jobExecutionDao.findByStateOrderByIdAsc(JobExecutionState.WAITING.name());
        if (waitingJobExecutions != null) {
            return waitingJobExecutions.stream().map(
                    jobExecutionEntity -> jobExecutionConverter.convert(
                            new JSONObject(jobExecutionEntity.getContent()))).collect(Collectors.toList());
        }
        return new LinkedList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup(Long jobId, int numToKeep) {
        List<JobExecutionEntity> jobExecutionEntities = jobExecutionDao.findByJobIdOrderByIdDesc(jobId);
        if (jobExecutionEntities != null && jobExecutionEntities.size() > numToKeep) {
            jobExecutionDao.deleteByJobIdAndIdBefore(jobId, jobExecutionEntities.get(numToKeep -1).getId());
        }
    }

}
