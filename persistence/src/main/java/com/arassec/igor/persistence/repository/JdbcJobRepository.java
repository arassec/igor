package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.model.Job;
import com.arassec.igor.core.repository.JobRepository;
import com.arassec.igor.persistence.converter.JobConverter;
import com.arassec.igor.persistence.dao.JobDao;
import com.arassec.igor.persistence.entity.JobEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Andreas Sensen on 15.04.2017.
 */
@Component
@Transactional
public class JdbcJobRepository implements JobRepository {

    /**
     * DAO for job entities.
     */
    @Autowired
    private JobDao jobDao;

    @Autowired
    private JobConverter jobConverter;

    /**
     * Persists jobs using JDBC.
     *
     * @param job The job to persist.
     */
    @Override
    public void upsert(Job job) {

        JobEntity jobEntity;
        if (job.getId() == null) {
            jobEntity = new JobEntity();
        } else {
            Optional<JobEntity> jobEntityOptional = jobDao.findById(job.getId());
            if (!jobEntityOptional.isPresent()) {
                throw new IllegalStateException("No job with ID " + job.getId() + " available!");
            }
            jobEntity = jobEntityOptional.get();
        }
        jobEntity.setName(job.getName());
        jobEntity.setContent(jobConverter.convert(job));
        jobDao.save(jobEntity);
    }

    @Override
    public Job findById(Long id) {
        Optional<JobEntity> jobEntityOptional = jobDao.findById(id);
        if (jobEntityOptional.isPresent()) {
            Job job =  jobConverter.convert(jobEntityOptional.get().getContent());
            job.setId(id);
            return job;
        }
        return null;
    }

    @Override
    public List<Job> findAll() {
        List<Job> result = new LinkedList<>();
        for (JobEntity jobEntity : jobDao.findAll()) {
            Job job = jobConverter.convert(jobEntity.getContent());
            job.setId(jobEntity.getId());
            if (job != null) {
                result.add(job);
            }
        }
        return result;
    }

    @Override
    public void deleteById(Long id) {
        jobDao.deleteById(id);
    }

}
