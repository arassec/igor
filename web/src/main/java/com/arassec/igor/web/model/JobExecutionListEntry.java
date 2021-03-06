package com.arassec.igor.web.model;

import lombok.Data;

import java.time.Instant;

/**
 * A list entry for job executions.
 */
@Data
public class JobExecutionListEntry {

    /**
     * The job-execution's ID.
     */
    private Long id;

    /**
     * The job's ID.
     */
    private String jobId;

    /**
     * The name of the job.
     */
    private String jobName;

    /**
     * Contains the state of this duration.
     */
    private String state;

    /**
     * Contains the creation time of this execution. This must not be the starting time of the job!
     */
    private Instant created;

    /**
     * Contains the starting time of this job execution. Might be null if the execution has not yet been started.
     */
    private Instant started;

    /**
     * Contains the time this execution finished. Might be null if the execution has not yet finished.
     */
    private Instant finished;

    /**
     * Contains the formatted duration of this execution.
     */
    private String duration;

}
