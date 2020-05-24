package com.arassec.igor.module.misc.trigger;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.core.model.trigger.ScheduledTrigger;
import com.arassec.igor.module.misc.util.validation.ValidCronExpression;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

/**
 * Triggers jobs according to a configurable CRON expression.
 */
@IgorComponent
public class CronTrigger extends BaseUtilTrigger implements ScheduledTrigger {

    /**
     * The CRON expression that is used to trigger the job.
     */
    @Setter
    @NotEmpty
    @ValidCronExpression
    @IgorParam(subtype = ParameterSubtype.CRON)
    private String cronExpression;

    /**
     * Creates a new component instance.
     */
    public CronTrigger() {
        super("cron-trigger");
    }

    /**
     * Returns the configured CRON expression.
     *
     * @return The CRON expression.
     */
    @Override
    public String getCronExpression() {
        return cronExpression;
    }

}
