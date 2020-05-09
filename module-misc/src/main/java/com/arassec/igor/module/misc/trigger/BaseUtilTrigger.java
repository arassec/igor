package com.arassec.igor.module.misc.trigger;

import com.arassec.igor.core.model.trigger.BaseTrigger;

/**
 * Baseclass for Triggers.
 */
public abstract class BaseUtilTrigger extends BaseTrigger {

    /**
     * Creates a new component instance.
     *
     * @param typeId The type ID.
     */
    public BaseUtilTrigger(String typeId) {
        super("util-triggers", typeId);
    }

}
