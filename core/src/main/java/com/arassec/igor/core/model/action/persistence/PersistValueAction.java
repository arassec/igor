package com.arassec.igor.core.model.action.persistence;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.core.model.service.persistence.PersistenceService;

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
     * Either saves a value to the persistence store or adds a comment to the data.
     *
     * @param data     The data the action will work with.
     * @param isDryRun Set to {@code true} if the value should not actually be persisted. A comment is added instead.
     * @return Always {@code true}.
     */
    private boolean processInternal(IgorData data, boolean isDryRun) {
        if (isValid(data)) {
            if (isDryRun) {
                data.put("dryRunComment", "Saved: " + data.getJobId() + "/" + data.getTaskId() + "/" + data.get(dataKey));
            } else {
                service.save(data.getJobId(), data.getTaskId(), (String) data.get(dataKey));
            }
        }
        return true;
    }
}
