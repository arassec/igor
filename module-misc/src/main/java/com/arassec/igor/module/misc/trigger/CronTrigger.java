package com.arassec.igor.module.misc.trigger;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.core.model.trigger.ScheduledTrigger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Triggers jobs according to a configurable CRON expression.
 */
@Component
@Scope("prototype")
public class CronTrigger extends BaseMiscTrigger implements ScheduledTrigger {

    /**
     * The CRON expression that is used to trigger the job.
     */
    @IgorParam(subtype = ParameterSubtype.CRON)
    private String cronExpression;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeId() {
        return "c0f762f1-b192-4de1-8acd-b03f09262865";
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
