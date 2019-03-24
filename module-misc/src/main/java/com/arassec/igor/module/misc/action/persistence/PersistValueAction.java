package com.arassec.igor.module.misc.action.persistence;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.module.misc.service.persistence.PersistenceService;

/**
 * Persists a value from the supplied data to the persistence store.
 */
@IgorAction(label = "Persist value")
public class PersistValueAction extends BasePersistenceAction {

    /**
     * The service to use for persisting values.
     */
    @IgorParam
    private PersistenceService service;

    /**
     * The number of values to keep in the persistence store.
     */
    @IgorParam
    private int numValuesToKeep;

    /**
     * Takes the value from the supplied data and saves it to the persistence store.
     *
     * @param data The data the action will work with.
     * @return Always {@code true}.
     */
    @Override
    public boolean process(IgorData data) {
        return processInternal(data, false);
    }

    /**
     * Adds a comment to the data that a value would have been persisted to the persistence store.
     * <p>
     * The data is not actually persisted!
     *
     * @param data The data the action will work with.
     * @return Always {@code true}.
     */
    @Override
    public boolean dryRun(IgorData data) {
        return processInternal(data, true);
    }

    /**
     * Cleans up the persisted values and keep only the {@link #numValuesToKeep} most recent values in the store.
     *
     * @param jobId  The job's ID.
     * @param taskId The task's ID.
     */
    @Override
    public void complete(Long jobId, String taskId) {
        service.cleanup(jobId, taskId, numValuesToKeep);
    }

    /**
     * Either saves a value to the persistence store or adds a comment to the data.
     *
     * @param data     The data the action will work with.
     * @param isDryRun Set to {@code true} if the value should not actually be persisted. A comment is added instead.
     * @return Always {@code true}.
     */
    private boolean processInternal(IgorData data, boolean isDryRun) {
        if (isValid(data)) {
            if (isDryRun) {
                data.put(DRY_RUN_COMMENT_KEY, "Persisted: " + data.get(dataKey));
            } else if (!service.isPersisted(Long.valueOf(data.getJobId()), data.getTaskId(), (String) data.get(dataKey))) {
                service.save(Long.valueOf(data.getJobId()), data.getTaskId(), (String) data.get(dataKey));
            }
        }
        return true;
    }
}
