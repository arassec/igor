package com.arassec.igor.core.model.trigger;

import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.core.util.validation.ValidCronExpression;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

/**
 * Base for {@link ScheduledTrigger}s.
 */
public abstract class BaseScheduledTrigger extends BaseTrigger implements ScheduledTrigger {

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
     *
     * @param categoryId The category ID.
     * @param typeId     The type ID.
     */
    public BaseScheduledTrigger(String categoryId, String typeId) {
        super(categoryId, typeId);
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
