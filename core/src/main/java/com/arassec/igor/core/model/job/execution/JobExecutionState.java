package com.arassec.igor.core.model.job.execution;

/**
 * The state of a single job run.
 */
public enum JobExecutionState {

    /**
     * The job waits for its execution.
     */
    WAITING,

    /**
     * The job is currently running.
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
    FAILED,

    /**
     * The job finished abnormally but the error has been manually resolved.
     */
    RESOLVED

}
