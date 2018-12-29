package com.arassec.igor.core.model.job.execution;

/**
 * The state of a single job run.
 */
public enum JobExecutionState {

    /**
     * The job is currently isRunning.
     */
    RUNNING,

    /**
     * The job finished without errors.
     */
    FINISHED,

    /**
     * The job was cancelled manually.
     */
    CANCELLED,

    /**
     * The job finished abnormally with errors.
     */
    FAILED

}
