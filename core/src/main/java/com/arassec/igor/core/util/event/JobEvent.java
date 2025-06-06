package com.arassec.igor.core.util.event;

import com.arassec.igor.core.model.job.Job;

/**
 * Event containing a job and a type. Those events are sent if a job changes its execution state.
 *
 * @param type The type of event.
 * @param job  The job that triggered the event.
 */
public record JobEvent(JobEventType type, Job job) {
}
