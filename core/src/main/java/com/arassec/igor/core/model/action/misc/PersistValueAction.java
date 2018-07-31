package com.arassec.igor.core.model.action.misc;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.core.model.service.persistence.PersistenceService;

/**
 * TODO: Document class.
 */
@IgorAction
public class PersistValueAction extends BaseAction {

    /**
     * The service to use for persisting values.
     */
    @IgorParam
    private PersistenceService service;

    @Override
    public boolean process(IgorData data) {
        if (isValid(data)) {
            service.save(data.getJobId(), data.getTaskName(), (String) data.get(dataKey));
        }
        return true;
    }

}
