package com.arassec.igor.module.misc.trigger;

import com.arassec.igor.core.model.annotation.IgorComponent;

/**
 * Trigger for manual job executions.
 */
@IgorComponent
public class ManualTrigger extends BaseUtilTrigger {

    /**
     * Creates a new component instance.
     */
    public ManualTrigger() {
        super("manual-trigger");
    }

}
