package com.arassec.igor.plugin.core.util.trigger;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.trigger.BaseTrigger;
import com.arassec.igor.plugin.core.CorePluginCategory;
import com.arassec.igor.plugin.core.CorePluginType;

/**
 * Trigger for manual job executions.
 */
@IgorComponent
public class ManualTrigger extends BaseTrigger {

    /**
     * Creates a new component instance.
     */
    public ManualTrigger() {
        super(CorePluginCategory.UTIL.getId(), CorePluginType.MANUAL_TRIGGER.getId());
    }

}
