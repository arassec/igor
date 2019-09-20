package com.arassec.igor.core.model.trigger;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;

/**
 * Triggers jobs according to a configurable CRON expression.
 */
@IgorComponent("CRON")
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
