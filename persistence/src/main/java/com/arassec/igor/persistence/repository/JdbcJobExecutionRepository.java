package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.application.converter.JsonJobExecutionConverter;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.repository.JobExecutionRepository;
import com.arassec.igor.persistence.dao.JobExecutionDao;
import com.arassec.igor.persistence.entity.JobExecutionEntity;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    public JobExecution save(Long jobId, JobExecution jobExecution) {
        JobExecutionEntity entity = new JobExecutionEntity();
        entity.setJobId(jobId);
        entity.setContent(jobExecutionConverter.convert(jobExecution).toString());
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<JobExecution> findAllOfJob(Long jobId) {
        List<JobExecutionEntity> jobExecutionEntities = jobExecutionDao.findByJobId(jobId);
        if (jobExecutionEntities != null) {
            jobExecutionEntities.stream().map(jobExecutionEntity ->
                    jobExecutionConverter.convert(new JSONObject(jobExecutionEntity.getContent()))).collect(Collectors.toList());
        }
        return new LinkedList<>();
    }

}
