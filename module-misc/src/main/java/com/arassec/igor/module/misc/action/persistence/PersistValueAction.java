package com.arassec.igor.module.misc.action.persistence;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.PersistentValue;
import com.arassec.igor.core.repository.PersistentValueRepository;
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
        super("6d768a9f-8f25-4ac1-ad8a-f825fbd1465c", persistentValueRepository);
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

        String jobId = getJobId(data);
        String taskId = getTaskId(data);

        String resolvedInput = getString(data, input);
        if (resolvedInput == null) {
            log.debug("Not enough data to persist: {}", input);
            return List.of();
        }

        PersistentValue value = new PersistentValue(getString(data, resolvedInput));
        if (!persistentValueRepository.isPersisted(jobId, taskId, value)) {
            log.debug("Persisted: '{}'", value);
            persistentValueRepository.upsert(jobId, taskId, value);
        } else {
            log.debug("Already persisted: '{}'", value);
        }

        return List.of(data);
    }

    /**
     * Cleans up the persisted values and keep only the {@link #numValuesToKeep} most recent values in the store.
     *
     * @param jobId        The job's ID.
     * @param taskId       The task's ID.
     * @param jobExecution The container for job execution details.
     */
    @Override
    public void shutdown(String jobId, String taskId, JobExecution jobExecution) {
        if (numValuesToKeep > 0) {
            persistentValueRepository.cleanup(jobId, taskId, numValuesToKeep);
        }
    }

}
