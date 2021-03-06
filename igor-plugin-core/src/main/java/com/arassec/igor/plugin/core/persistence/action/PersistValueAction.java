package com.arassec.igor.plugin.core.persistence.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.PersistentValue;
import com.arassec.igor.core.repository.PersistentValueRepository;
import com.arassec.igor.plugin.core.CorePluginType;
import com.arassec.igor.plugin.core.CorePluginUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * Persists a value from the supplied data to the persistence store.
 */
@Slf4j
@IgorComponent
public class PersistValueAction extends BasePersistenceAction {

    /**
     * The input to persist.
     */
    @Getter
    @Setter
    @NotBlank
    @IgorParam
    private String input;

    /**
     * The number of values to keep in the persistence store. The cleanup is only triggered if a value greater zero is
     * configured.
     */
    @Getter
    @Setter
    @IgorParam
    private int numValuesToKeep = 0;

    /**
     * Creates a new instance.
     *
     * @param persistentValueRepository The repository for persisted values.
     */
    public PersistValueAction(PersistentValueRepository persistentValueRepository) {
        super(CorePluginType.PERSIST_VALUE_ACTION.getId(), persistentValueRepository);
    }

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

        var jobId = getJobId(data);

        var resolvedInput = CorePluginUtils.getString(data, input);
        if (resolvedInput == null) {
            log.debug("Not enough data to persist: {}", input);
            return List.of();
        }

        var value = new PersistentValue(CorePluginUtils.getString(data, resolvedInput));
        if (!persistentValueRepository.isPersisted(jobId, value)) {
            if (isSimulation(data)) {
                data.put(DataKey.SIMULATION_LOG.getKey(), "Would have persisted: " + value.getContent());
            } else {
                log.debug("Persisted: '{}'", value);
                persistentValueRepository.upsert(jobId, value);
            }
        } else {
            log.debug("Already persisted: '{}'", value);
        }

        return List.of(data);
    }

    /**
     * Cleans up the persisted values and keep only the {@link #numValuesToKeep} most recent values in the store.
     *
     * @param jobExecution The container for job execution details.
     */
    @Override
    public void shutdown(JobExecution jobExecution) {
        if (numValuesToKeep > 0) {
            persistentValueRepository.cleanup(jobExecution.getJobId(), numValuesToKeep);
        }
    }

}
