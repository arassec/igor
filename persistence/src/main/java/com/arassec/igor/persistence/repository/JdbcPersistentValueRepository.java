package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.model.job.misc.PersistentValue;
import com.arassec.igor.core.repository.PersistentValueRepository;
import com.arassec.igor.persistence.dao.PersistentValueDao;
import com.arassec.igor.persistence.entity.PersistentValueEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * JDBC repository for persistent values.
 */
@Component
@Transactional
@RequiredArgsConstructor
public class JdbcPersistentValueRepository implements PersistentValueRepository {

    /**
     * DAO for Access to persistent values.
     */
    private final PersistentValueDao persistentValueDao;

    /**
     * Saves the supplied value in igor's own database.
     *
     * @param jobId  The job's ID.
     * @param taskId The task's ID.
     * @param value  The value to save.
     *
     * @return The persisted value. An ID is added if required.
     */
    @Override
    public PersistentValue upsert(String jobId, String taskId, PersistentValue value) {
        PersistentValueEntity entity = new PersistentValueEntity();
        if (value.getId() != null) {
            entity = persistentValueDao.findById(value.getId()).orElseThrow(
                    () -> new IllegalStateException("No persistent value for ID " + value.getId() + " available!"));
        } else {
            entity.setCreated(Instant.now());
            entity.setJobId(jobId);
            entity.setTaskId(taskId);
        }
        entity.setContent(value.getContent());

        PersistentValueEntity persistedEntity = persistentValueDao.save(entity);
        if (value.getId() == null) {
            value.setId(persistedEntity.getId());
            value.setCreated(entity.getCreated());
        }

        return value;
    }

    /**
     * Checks in igor's database if the value is already persisted.
     *
     * @param jobId  The job's ID.
     * @param taskId The task's ID.
     * @param value  The value to check.
     *
     * @return {@code true} if the value is persisted, {@code false} otherwise.
     */
    @Override
    public boolean isPersisted(String jobId, String taskId, PersistentValue value) {
        if (value == null) {
            return false;
        }
        return (persistentValueDao.findByJobIdAndTaskIdAndContent(jobId, taskId, value.getContent()) != null);
    }

    /**
     * Deletes old entries from the database.
     *
     * @param jobId            The job's ID.
     * @param taskId           The task's ID.
     * @param numEntriesToKeep Number of entries to keep.
     */
    @Override
    public void cleanup(String jobId, String taskId, int numEntriesToKeep) {
        List<Integer> ids = persistentValueDao.findMostRecentIds(jobId, taskId, numEntriesToKeep);
        if (ids != null && ids.size() == numEntriesToKeep) {
            Integer oldestIdToKeep = ids.get(numEntriesToKeep - 1);
            persistentValueDao.deleteByJobIdAndTaskIdAndIdBefore(jobId, taskId, Long.valueOf(oldestIdToKeep));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteByJobId(String jobId) {
        if (jobId != null) {
            persistentValueDao.deleteByJobId(jobId);
        }
    }

}
