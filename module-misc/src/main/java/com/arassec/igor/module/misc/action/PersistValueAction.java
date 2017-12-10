package com.arassec.igor.module.misc.action;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.module.misc.service.PersistenceService;

/**
 * TODO: Document class.
 */
@IgorAction(type = "com.arassec.igor.action.misc.PersistValueAction")
public class PersistValueAction extends BaseAction {

    /**
     * The service to use for persisting values.
     */
    @IgorParam
    private PersistenceService service;

    @Override
    public boolean process(IgorData data) {
        if (isValid(data)) {
            service.save((String) data.get(dataKey));
        }
        return true;
    }

}
