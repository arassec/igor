package com.arassec.igor.module.misc.action.persistence;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.module.misc.service.persistence.PersistenceService;

/**
 * Filters data of which a value has already been persisted before.
 */
@IgorAction(label = "Filter persisted value")
public class FilterPersistedValueAction extends BasePersistenceAction {

    /**
     * The service to use for persisting values.
     */
    @IgorParam
    private PersistenceService service;

    /**
     * Retrieves the value from the supplied data and searches it in the persisted values. If it is already persisted, the data
     * is ignored.
     *
     * @param data The data the action will work with.
     */
    @Override
    public boolean process(IgorData data) {
        if (isValid(data)) {
            return !service.isPersisted(data.getJobId(), data.getTaskId(), (String) data.get(dataKey));
        }
        return true;
    }

}
