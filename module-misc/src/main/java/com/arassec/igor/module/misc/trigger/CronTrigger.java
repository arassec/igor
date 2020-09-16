package com.arassec.igor.module.misc.trigger;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.trigger.BaseScheduledTrigger;
import com.arassec.igor.module.misc.MiscConstants;

/**
 * Triggers jobs according to a configurable CRON expression.
 */
@IgorComponent
public class CronTrigger extends BaseScheduledTrigger {

    /**
     * Creates a new component instance.
     */
    public CronTrigger() {
        super(MiscConstants.TRIGGER_CATEGORY_ID,"cron-trigger");
    }

}
