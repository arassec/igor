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

    private Instant started;

    private Instant finished;

    private JobExecutionState executionState;

    private Throwable errorCause;

    private String currentTask;

    public synchronized void cancel() {
        this.executionState = JobExecutionState.CANCELLED;
        this.finished = Instant.now();
    }

    public synchronized void fail(Throwable errorCause) {
        this.executionState = JobExecutionState.FAILED;
        this.errorCause = errorCause;
        this.finished = Instant.now();
    }

    public boolean cancelled() {
        return !JobExecutionState.RUNNING.equals(executionState);
    }

}
