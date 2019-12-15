package com.arassec.igor.core.model.job.execution;

import com.arassec.igor.core.util.StacktraceFormatter;
import lombok.Data;
import lombok.ToString;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains information about a single job run.
 */
@Data
@ToString
public class JobExecution {

    /**
     * The job-execution's ID.
     */
    private Long id;

    /**
     * The job's ID.
     */
    private String jobId;

    /**
     * The timestamp of the creation of this job-execution.
     */
    private Instant created;

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
    private String errorCause;

    /**
     * Contains the current task the job is in.
     */
    private String currentTask;

    /**
     * List with current work in progress, that should be visible in the UI for the user.
     */
    private final List<WorkInProgressMonitor> workInProgress = Collections.synchronizedList(new LinkedList<>());

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
        if (errorCause != null) {
            this.errorCause = StacktraceFormatter.format(errorCause);
        }
        this.finished = Instant.now();
    }

    /**
     * Returns whether the job is currently running or not.
     *
     * @return {@code true} if the job is running, {@code false} otherwise.
     */
    public boolean isRunning() {
        return JobExecutionState.RUNNING.equals(executionState);
    }

    /**
     * Adds a {@link WorkInProgressMonitor} to the current list of work-in-progress.
     *
     * @param workInProgressMonitor The new monitor to add.
     */
    public void addWorkInProgress(WorkInProgressMonitor workInProgressMonitor) {
        workInProgress.add(workInProgressMonitor);
    }

    /**
     * Removes a {@link WorkInProgressMonitor} from the current list of work-in-progress.
     *
     * @param workInProgressMonitor The monitor to remove.
     */
    public void removeWorkInProgress(WorkInProgressMonitor workInProgressMonitor) {
        workInProgress.remove(workInProgressMonitor);
    }

    /**
     * Returns the current work-in-progress.
     *
     * @return List of {@link WorkInProgressMonitor} items.
     */
    public List<WorkInProgressMonitor> getWorkInProgress() {
        synchronized (workInProgress) {
            return new LinkedList<>(workInProgress);
        }
    }

}
