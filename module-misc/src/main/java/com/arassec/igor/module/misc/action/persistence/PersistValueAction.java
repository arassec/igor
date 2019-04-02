package com.arassec.igor.module.misc.action.persistence;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.persistence.PersistentValue;
import com.arassec.igor.core.model.provider.IgorData;

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
        if (numValuesToKeep > 0) {
            persistentValueRepository.cleanup(jobId, taskId, numValuesToKeep);
        }
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
            } else if (!persistentValueRepository.isPersisted(Long.valueOf(data.getJobId()), data.getTaskId(), new PersistentValue((String) data.get(dataKey)))) {
                persistentValueRepository.upsert(Long.valueOf(data.getJobId()), data.getTaskId(), new PersistentValue((String) data.get(dataKey)));
            }
        }
        return true;
    }
}
