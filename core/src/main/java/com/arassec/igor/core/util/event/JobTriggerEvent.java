package com.arassec.igor.core.util.event;

import com.arassec.igor.core.model.trigger.EventType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * An event triggering a job.
 */
@Getter
@RequiredArgsConstructor
public class JobTriggerEvent {

    /**
     * The ID of the job that should be triggered.
     */
    private final String jobId;

    /**
     * Optional event data.
     */
    private final Map<String, Object> eventData;

    /**
     * The type of the event.
     */
    private final EventType eventType;

}
