package com.arassec.igor.plugin.common.util.trigger;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.trigger.BaseScheduledTrigger;
import com.arassec.igor.plugin.common.CommonCategory;


/**
 * Triggers jobs according to a configurable CRON expression.
 */
@IgorComponent
public class CronTrigger extends BaseScheduledTrigger {

    /**
     * Creates a new component instance.
     */
    public CronTrigger() {
        super(CommonCategory.UTIL.getId(), "cron-trigger");
    }

}
