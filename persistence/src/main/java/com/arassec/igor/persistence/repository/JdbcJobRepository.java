package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.application.converter.JsonJobConverter;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.repository.JobRepository;
import com.arassec.igor.persistence.dao.JobDao;
import com.arassec.igor.persistence.entity.JobEntity;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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
        JobEntity jobEntity;
        if (job.getId() == null) {
            jobEntity = new JobEntity();
        } else {
            jobEntity = jobDao.findById(job.getId()).orElseThrow(
                    () -> new IllegalStateException("No job with ID " + job.getId() + " available!"));
        }
        jobEntity.setName(job.getName());
        jobEntity.setContent(jsonJobConverter.convert(job, true, false).toString());
        JobEntity savedJob = jobDao.save(jobEntity);
        job.setId(savedJob.getId());
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
            Job job = jsonJobConverter.convert(new JSONObject(jobEntityOptional.get().getContent()), true);
            job.setId(id);
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
            Job job = jsonJobConverter.convert(new JSONObject(jobEntity.getContent()), true);
            job.setId(jobEntity.getId());
            if (job != null) {
                result.add(job);
            }
        }
        return result;
    }

    /**
     * Deletes a job by its ID.
     *
     * @param id The ID of the job to delete.
     */
    @Override
    public void deleteById(Long id) {
        jobDao.deleteById(id);
    }

}
