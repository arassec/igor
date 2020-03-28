package com.arassec.igor.web.model;

import lombok.Data;

/**
 * Overview over job executions.
 */
@Data
public class JobExecutionOverview {

    /**
     * The number of job slots for running jobs.
     */
    private int numSlots;

    /**
     * The number of currently running jobs.
     */
    private int numRunning;

    /**
     * The number of currently waiting jobs.
     */
    private int numWaiting;

    /**
     * The number of failed jobs.
     */
    private int numFailed;

}
