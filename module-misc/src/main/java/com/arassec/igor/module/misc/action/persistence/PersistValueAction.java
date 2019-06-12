package com.arassec.igor.module.misc.action.persistence;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.persistence.PersistentValue;

import java.util.List;
import java.util.Map;

/**
 * Persists a value from the supplied data to the persistence store.
 */
@IgorAction(label = "Persist value")
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
     * @param isDryRun     If set to {@code true}, the values are not actually persisted.
     * @param jobExecution The job execution log.
     * @return Always {@code true}.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, boolean isDryRun, JobExecution jobExecution) {
        if (isValid(data)) {
            Long jobId = getLong(data, JOB_ID_KEY);
            String taskId = getString(data, TASK_ID_KEY);
            PersistentValue value = new PersistentValue(getString(data, dataKey));
            if (!persistentValueRepository.isPersisted(jobId, taskId, value)) {
                if (isDryRun) {
                    data.put(DRY_RUN_COMMENT_KEY, "Persisted: " + data.get(dataKey));
                } else {
                    persistentValueRepository.upsert(jobId, taskId, value);
                }
                return List.of(data);
            } else {
                if (isDryRun) {
                    data.put(DRY_RUN_COMMENT_KEY, data.get(dataKey) + " already persisted!");
                }
            }
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
