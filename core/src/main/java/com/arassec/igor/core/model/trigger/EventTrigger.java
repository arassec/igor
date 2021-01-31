package com.arassec.igor.core.model.trigger;

import com.arassec.igor.core.model.job.misc.ProcessingFinishedCallback;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Defines a {@link Trigger}, that consumes events and hands them over to an active job for processing.
 */
public interface EventTrigger extends Trigger, ProcessingFinishedCallback {

    /**
     * Sets the queue to put events in on arrival.
     *
     * @param queue The job's input queue. Entries put into the queue will be processed by the job containing the trigger.
     */
    void setEventQueue(BlockingQueue<Map<String, Object>> queue);

    /**
     * Processes the supplied event data and hands it over to the job.
     *
     * @param eventData The event's data.
     */
    void processEvent(Map<String, Object> eventData);

    /**
     * Returns the event type  supported by this trigger.
     *
     * @return The supported event type.
     */
    default EventType getSupportedEventType() {
        return EventType.NONE;
    }

}
