package com.arassec.igor.module.misc.action.persistence;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.PersistentValue;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Persists a value from the supplied data to the persistence store.
 */
@Slf4j
@IgorComponent("Persist value")
public class PersistValueAction extends BasePersistenceAction {

    /**
     * The number of values to keep in the persistence store.
     */
    @IgorParam
    private int numValuesToKeep;

    /**
     * Takes the value from the supplied data and saves it to the persistence store.
     *
     * @param data         The data the action will work with.
     * @param jobExecution The job execution log.
     *
     * @return Always {@code true}.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        if (isValid(data)) {
            Long jobId = getLong(data, JOB_ID_KEY);
            String taskId = getString(data, TASK_ID_KEY);
            PersistentValue value = new PersistentValue(getString(data, dataKey));
            if (!persistentValueRepository.isPersisted(jobId, taskId, value)) {
                log.debug("Persisted: '{}'", value);
                persistentValueRepository.upsert(jobId, taskId, value);
            } else {
                log.debug("Already persisted: '{}'", value);
            }
            return List.of(data);
        }
        return null;
    }

    /**
     * Cleans up the persisted values and keep only the {@link #numValuesToKeep} most recent values in the store.
     *
     * @param jobId  The job's ID.
     * @param taskId The task's ID.
     */
    @Override
    public void shutdown(Long jobId, String taskId) {
        if (numValuesToKeep > 0) {
            persistentValueRepository.cleanup(jobId, taskId, numValuesToKeep);
        }
    }

}
