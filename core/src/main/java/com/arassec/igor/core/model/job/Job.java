package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.job.dryrun.DryRunJobResult;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.util.StacktraceFormatter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Defines an igor job.
 */
@Data
@Slf4j
public class Job {

    /**
     * The job's ID.
     */
    private Long id;

    /**
     * The job's name.
     */
    private String name;

    /**
     * A job description.
     */
    private String description;

    /**
     * A trigger for the job.
     */
    private Trigger trigger;

    /**
     * Enables or disables the job.
     */
    private boolean active;

    /**
     * Max. number of job-execution entries to keep for this job.
     */
    private int executionHistoryLimit = 5;

    /**
     * The tasks this job will perform.
     */
    private List<Task> tasks = new LinkedList<>();

    /**
     * Contains information about a job run.
     */
    private JobExecution currentJobExecution;

    /**
     * Indicates whether the job is running or not.
     */
    private boolean running = false;

    /**
     * Runs the job.
     */
    public void run(JobExecution jobExecution) {
        log.debug("Running job: {} ({})", name, id);
        currentJobExecution = Objects.requireNonNullElseGet(jobExecution, JobExecution::new);
        currentJobExecution.setExecutionState(JobExecutionState.RUNNING);
        currentJobExecution.setStarted(Instant.now());
        running = true;
        try {
            for (Task task : tasks) {
                if (!currentJobExecution.isRunning()) {
                    break;
                }
                jobExecution.setCurrentTask(task.getName());
                if (task.isActive()) {
                    task.run(id, currentJobExecution);
                }
            }
            if (currentJobExecution.isRunning()) {
                currentJobExecution.setExecutionState(JobExecutionState.FINISHED);
            }
        } catch (Exception e) {
            log.error("Exception during job execution!", e);
            currentJobExecution.fail(e);
        } finally {
            currentJobExecution.setFinished(Instant.now());
            log.debug("Finished job: {} ({}): {}", name, id, currentJobExecution);
            running = false;
        }
    }

    /**
     * Performs a dry-run of the job.
     *
     * @return The result data created during job execution.
     */
    public DryRunJobResult dryRun() {
        DryRunJobResult result = new DryRunJobResult();
        log.debug("Dry-running job: {}", name);
        try {
            tasks.forEach(task -> task.dryRun(result, id));
        } catch (Exception e) {
            result.setErrorCause(StacktraceFormatter.format(e));
        }
        log.debug("Finished dry-run of job: {}", name);
        return result;
    }

    /**
     * Cancels the job if it is currently running.
     */
    public void cancel() {
        if (currentJobExecution != null && JobExecutionState.RUNNING.equals(currentJobExecution.getExecutionState())) {
            currentJobExecution.setExecutionState(JobExecutionState.CANCELLED);
        }
    }
}
