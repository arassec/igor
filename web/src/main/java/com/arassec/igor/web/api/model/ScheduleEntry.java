package com.arassec.igor.web.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Models an entry for the job schedule list.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleEntry {

    /**
     * The job's ID.
     */
    private Long jobId;

    /**
     * The job's name.
     */
    private String jobName;

    /**
     * The instant at which the job will next be executed.
     */
    private Instant nextRun;

}
