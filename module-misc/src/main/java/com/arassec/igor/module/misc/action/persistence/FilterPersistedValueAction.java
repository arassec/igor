package com.arassec.igor.module.misc.action.persistence;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.persistence.PersistentValue;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Filters data of which a value has already been persisted before.
 */
@Slf4j
@IgorComponent("Filter persisted value")
public class FilterPersistedValueAction extends BasePersistenceAction {

    /**
     * Retrieves the value from the supplied data and searches it in the persisted values. If it is already persisted, the data is
     * ignored.
     *
     * @param data         The data the action will work with.
     * @param jobExecution The job execution log.
     *
     * @return The manipulated data.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        if (isValid(data)) {
            Long jobId = getLong(data, JOB_ID_KEY);
            String taskId = getString(data, TASK_ID_KEY);
            String rawValue = getString(data, dataKey);
            PersistentValue value = new PersistentValue(rawValue);
            if (persistentValueRepository.isPersisted(jobId, taskId, value)) {
                log.debug("Filtered persisted value: '{}'", rawValue);
                return null;
            }
            log.debug("Passed un-persisted value: '{}'", rawValue);
            return List.of(data);
        }
        return null;
    }

}
