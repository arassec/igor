package com.arassec.igor.core.model.trigger;

/**
 * Defines a {@link Trigger}, that is scheduled by igor on a regular basis.
 */
public interface ScheduledTrigger extends Trigger {

    /**
     * The trigger schedule as CRON expression.
     *
     * @return The schedule as CRON expression.
     */
    String getCronExpression();

}
