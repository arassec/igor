package com.arassec.igor.plugin.core.persistence.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.PersistentValue;
import com.arassec.igor.core.repository.PersistentValueRepository;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.CorePluginUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * <h2>'Filter Persisted Value' Action</h2>
 *
 * <h3>Description</h3>
 * This action filters data items based on values from igor's datastore.<br>
 *
 * Filtered data items are not passed to the following action.
 */
@Slf4j
@Getter
@Setter
@IgorComponent(typeId = "filter-persisted-value-action", categoryId = CoreCategory.PERSISTENCE)
public class FilterPersistedValueAction extends BasePersistenceAction {

    /**
     * A mustache expression selecting a property from the data item. The property's value is checked against all persisted values
     * from igor's datastore. If the value is already persisted, the data item is filtered.
     */
    @NotBlank
    @IgorParam
    private String input;

    /**
     * Creates a new instance.
     *
     * @param persistentValueRepository The repository for persisted values.
     */
    public FilterPersistedValueAction(PersistentValueRepository persistentValueRepository) {
        super(persistentValueRepository);
    }

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

        String jobId = getJobId(data);
        var resolvedInput = CorePluginUtils.getString(data, input);

        if (resolvedInput == null) {
            log.debug("Not enough data to filter: {}", input);
            return List.of();
        }

        var value = new PersistentValue(resolvedInput);
        if (persistentValueRepository.isPersisted(jobId, value)) {
            log.debug("Filtered persisted value: '{}'", resolvedInput);
            return List.of();
        }

        log.debug("Passed un-persisted value: '{}'", resolvedInput);
        return List.of(data);
    }

}
