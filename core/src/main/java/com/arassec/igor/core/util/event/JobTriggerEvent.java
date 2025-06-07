package com.arassec.igor.core.util.event;

import com.arassec.igor.core.model.trigger.EventType;

import java.util.Map;

/**
 * An event triggering a job.
 *
 * @param jobId     The ID of the job that should be triggered.
 * @param eventData Optional event data.
 * @param eventType The type of the event.
 */
public record JobTriggerEvent(String jobId, Map<String, Object> eventData, EventType eventType) {
}
