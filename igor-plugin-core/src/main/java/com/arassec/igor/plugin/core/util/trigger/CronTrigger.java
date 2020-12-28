package com.arassec.igor.plugin.core.util.trigger;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.trigger.BaseScheduledTrigger;
import com.arassec.igor.plugin.core.CoreCategory;


/**
 * Triggers jobs according to a configurable CRON expression.
 */
@IgorComponent
public class CronTrigger extends BaseScheduledTrigger {

    /**
     * Creates a new component instance.
     */
    public CronTrigger() {
        super(CoreCategory.UTIL.getId(), "cron-trigger");
    }

}
