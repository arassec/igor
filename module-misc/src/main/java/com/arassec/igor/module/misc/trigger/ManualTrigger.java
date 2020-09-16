package com.arassec.igor.module.misc.trigger;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.trigger.BaseTrigger;
import com.arassec.igor.module.misc.MiscConstants;

/**
 * Trigger for manual job executions.
 */
@IgorComponent
public class ManualTrigger extends BaseTrigger {

    /**
     * Creates a new component instance.
     */
    public ManualTrigger() {
        super(MiscConstants.TRIGGER_CATEGORY_ID, "manual-trigger");
    }

}
