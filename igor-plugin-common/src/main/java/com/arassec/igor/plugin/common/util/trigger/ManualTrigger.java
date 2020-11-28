package com.arassec.igor.plugin.common.util.trigger;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.trigger.BaseTrigger;
import com.arassec.igor.plugin.common.CommonCategory;

/**
 * Trigger for manual job executions.
 */
@IgorComponent
public class ManualTrigger extends BaseTrigger {

    /**
     * Creates a new component instance.
     */
    public ManualTrigger() {
        super(CommonCategory.UTIL.getId(), "manual-trigger");
    }

}
