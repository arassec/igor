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
        JobEntity jobEntity = jobDao.findOne(job.getId());
        if (jobEntity == null) {
            jobEntity = new JobEntity();
            jobEntity.setId(job.getId());
        }
        jobEntity.setContent(jobConverter.convert(job));
        jobDao.save(jobEntity);
    }

    @Override
    public List<Job> findAll() {
        List<Job> result = new LinkedList<>();
        for (JobEntity jobEntity : jobDao.findAll()) {
            Job job = jobConverter.convert(jobEntity.getContent());
            if (job != null) {
                result.add(job);
            }
        }
        return result;
    }

    @Override
    public Job findById(String id) {
        JobEntity jobEntity = jobDao.findOne(id);
        return jobConverter.convert(jobEntity.getContent());
    }

}
