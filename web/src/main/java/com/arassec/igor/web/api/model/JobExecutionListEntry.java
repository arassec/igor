package com.arassec.igor.web.api.model;

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
    private Long jobId;

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
     * Contains the formatted duration of this execution.
     */
    private String duration;

}
