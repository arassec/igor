package com.arassec.igor.core.model.action.misc;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.core.model.service.persistence.PersistenceService;

import java.util.List;

/**
 * Action that filters data of which a value has already been persisted.
 */
@IgorAction(label = "Filter persisted value")
public class FilterPersistedValueAction extends BaseAction {

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
            return !service.isPersisted(data.getJobId(), data.getTaskName(), (String) data.get(dataKey));
        }
        return true;
    }

}
