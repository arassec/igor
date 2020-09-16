package com.arassec.igor.core.model.trigger;

import java.util.Map;
import java.util.Queue;

/**
 * Defines a {@link Trigger}, that consumes events and hands them over to an active job for processing.
 */
public interface EventTrigger extends Trigger {

    /**
     * Sets the queue to put events in on arrival.
     *
     * @param queue The job's input queue. Entries put into the queue will be processed by the job containing the trigger.
     */
    void setEventQueue(Queue<Map<String, Object>> queue);

    /**
     * Processes the supplied event data and hands it over to the job.
     *
     * @param eventData The event's data.
     */
    void processEvent(Map<String, Object> eventData);

    /**
     * Returns simulation data. Used during simulated job executions to simulate the event data normally received by the trigger.
     *
     * @return A JSON-Object containing event-like data for simulated job executions.
     */
    Map<String, Object> getSimulationData();

}
