package com.arassec.igor.core.util.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * An event triggering a job.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JobTriggerEvent {

    /**
     * The ID of the job that should be triggered.
     */
    private String jobId;

    /**
     * Optional event data.
     */
    private Map<String, Object> eventData = new HashMap<>();

}
