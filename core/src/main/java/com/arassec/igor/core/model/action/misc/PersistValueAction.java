package com.arassec.igor.core.model.action.misc;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.dryrun.DryRunActionResult;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.core.model.service.persistence.PersistenceService;

/**
 * TODO: Document class.
 */
@IgorAction(label = "Persist value")
public class PersistValueAction extends BaseAction {

    /**
     * The service to use for persisting values.
     */
    @IgorParam
    private PersistenceService service;

    @Override
    public boolean process(IgorData data) {
        return processInternal(data, false);
    }

    @Override
    public boolean dryRun(IgorData data) {
        return processInternal(data, true);
    }

    private boolean processInternal(IgorData data, boolean isDryRun) {
        if (isValid(data)) {
            if (isDryRun) {
                data.put("dryRunComment", "Saved: " + data.getJobId() + "/" + data.getTaskName() + "/" + data.get(dataKey));
            } else {
                service.save(data.getJobId(), data.getTaskName(), (String) data.get(dataKey));
            }
        }
        return true;
    }
}
