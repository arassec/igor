package com.arassec.igor.core.model.action.persistence;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.core.model.service.persistence.PersistenceService;

/**
 * Cleans up persisted values. This is used for housekeeping so that old, persisted values can be removed from the
 * persistence store if they are not needed any more.
 */
@IgorAction(label = "Cleanup persisted values")
public class CleanupPersistedValuesAction extends BasePersistenceAction {

    /**
     * The persistence service to use.
     */
    @IgorParam
    private PersistenceService persistenceService;

    /**
     * The number of values to keep in the persistence store.
     */
    @IgorParam
    private int numValuesToKeep;

    /**
     * Key into the {@link IgorData} that identifies the property to process.
     */
    private String dataKey = "data";

    /**
     * Defines the number of threads the action should be processed with.
     */
    private int numThreads = DEFAULT_THREADS;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(IgorData data) {
        return true;
    }

    /**
     * Cleans up the persisted values and keepy only the {@link CleanupPersistedValuesAction#numValuesToKeep} most
     * recent values in the store.
     *
     * @param jobId  The job's ID.
     * @param taskId The task's ID.
     */
    @Override
    public void complete(Long jobId, String taskId) {
        persistenceService.cleanup(jobId, taskId, numValuesToKeep);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumThreads() {
        return 1;
    }

}
