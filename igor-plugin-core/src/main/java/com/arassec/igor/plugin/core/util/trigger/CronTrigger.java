package com.arassec.igor.plugin.core.util.trigger;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.core.model.trigger.BaseTrigger;
import com.arassec.igor.core.model.trigger.ScheduledTrigger;
import com.arassec.igor.plugin.core.CorePluginCategory;
import com.arassec.igor.plugin.core.CorePluginType;
import com.arassec.igor.plugin.core.validation.ValidCronExpression;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;


/**
 * Triggers jobs according to a configurable CRON expression.
 */
@IgorComponent
public class CronTrigger extends BaseTrigger implements ScheduledTrigger {

    /**
     * The CRON expression that is used to trigger the job.
     */
    @Setter
    @NotEmpty
    @ValidCronExpression
    @IgorParam(subtype = ParameterSubtype.CRON)
    protected String cronExpression;

    /**
     * Creates a new component instance.
     */
    public CronTrigger() {
        super(CorePluginCategory.UTIL.getId(), CorePluginType.CRON_TRIGGER.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCronExpression() {
        return cronExpression;
    }

}
