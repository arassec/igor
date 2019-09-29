package com.arassec.igor.module.misc.action.persistence;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.PersistentValue;
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
     * The input (query) to persist.
     */
    @IgorParam
    private String input;

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

        Long jobId = getJobId(data);
        String taskId = getTaskId(data);

        String resolvedInput = getString(data, input);

        if (resolvedInput == null) {
            log.debug("Not enough data to filter: {}", input);
            return null;
        }

        PersistentValue value = new PersistentValue(resolvedInput);
        if (persistentValueRepository.isPersisted(jobId, taskId, value)) {
            log.debug("Filtered persisted value: '{}'", resolvedInput);
            return null;
        }

        log.debug("Passed un-persisted value: '{}'", resolvedInput);
        return List.of(data);
    }

}
