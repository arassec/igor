package com.arassec.igor.core.model.trigger;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.IgorTrigger;
import com.arassec.igor.core.model.misc.ParameterSubtype;

/**
 * Triggers jobs according to a configurable CRON expression.
 */
@IgorTrigger(label = "CRON")
public class CronTrigger extends BaseTrigger {

    /**
     * The CRON expression that is used to trigger the job.
     */
    @IgorParam(subtype = ParameterSubtype.CRON)
    private String cronExpression;

    /**
     * Returns the configured CRON expression.
     *
     * @return The CRON expression.
     */
    public String getCronExpression() {
        return cronExpression;
    }

}
