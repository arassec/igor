package com.arassec.igor.core.model.job.execution;

import lombok.Data;
import lombok.ToString;

import java.time.Instant;

/**
 * Contains information about a single job run.
 */
@Data
@ToString
public class JobExecution {

    /**
     * Contains the start time of the job.
     */
    private Instant started;

    /**
     * Contains the finish time of the job.
     */
    private Instant finished;

    /**
     * The job's state.
     */
    private JobExecutionState executionState;

    /**
     * Might contain an error cause if the job finished abnormally.
     */
    private Throwable errorCause;

    /**
     * Contains the current task the job is in.
     */
    private String currentTask;

    /**
     * Cancels the job by setting the state accordingly.
     */
    public synchronized void cancel() {
        this.executionState = JobExecutionState.CANCELLED;
        this.finished = Instant.now();
    }

    /**
     * Stops the job run by setting the state to {@link JobExecutionState#FAILED}.
     *
     * @param errorCause Optional error causing the job to fail.
     */
    public synchronized void fail(Throwable errorCause) {
        this.executionState = JobExecutionState.FAILED;
        this.errorCause = errorCause;
        this.finished = Instant.now();
    }

    /**
     * Returns whether the job is currently isRunning or not.
     *
     * @return {@code true} if the job is isRunning, {@code false} otherwise.
     */
    public boolean isRunning() {
        return JobExecutionState.RUNNING.equals(executionState);
    }

}
