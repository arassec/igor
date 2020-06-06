package com.arassec.igor.core.util.event;

import com.arassec.igor.core.model.job.Job;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Event containing a job and a type. Those events are sent if a job changes its execution state.
 */
@Data
@AllArgsConstructor
public class JobEvent {

    /**
     * The type of event.
     */
    private final JobEventType type;

    /**
     * The job that triggered the event.
     */
    private final Job job;

}
