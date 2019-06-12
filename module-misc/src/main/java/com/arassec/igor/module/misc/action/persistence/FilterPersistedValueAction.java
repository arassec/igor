package com.arassec.igor.module.misc.action.persistence;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.persistence.PersistentValue;

import java.util.List;
import java.util.Map;

/**
 * Filters data of which a value has already been persisted before.
 */
@IgorAction(label = "Filter persisted value")
public class FilterPersistedValueAction extends BasePersistenceAction {

    /**
     * Retrieves the value from the supplied data and searches it in the persisted values. If it is already persisted, the data
     * is ignored.
     *
     * @param data         The data the action will work with.
     * @param isDryRun     Unused - always filters persisted values.
     * @param jobExecution The job execution log.
     * @return The manipulated data.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, boolean isDryRun, JobExecution jobExecution) {
        if (isValid(data)) {
            Long jobId = getLong(data, JOB_ID_KEY);
            String taskId = getString(data, TASK_ID_KEY);
            PersistentValue value = new PersistentValue(getString(data, dataKey));
            if (persistentValueRepository.isPersisted(jobId, taskId, value)) {
                return null;
            }
        }
        return List.of(data);
    }

}
