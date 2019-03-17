package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.job.dryrun.DryRunJobResult;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

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
     * The cron-expression that defines when the job is triggered.
     */
    private String trigger;

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
     * Runs the job.
     */
    public void run(JobExecution jobExecution) {
        log.debug("Running job: {} ({})", name, id);
        if (jobExecution == null) {
            currentJobExecution = new JobExecution();
        } else {
            currentJobExecution = jobExecution;
        }
        currentJobExecution.setExecutionState(JobExecutionState.RUNNING);
        currentJobExecution.setStarted(Instant.now());
        try {
            for (Task task : tasks) {
                if (!currentJobExecution.isRunning()) {
                    break;
                }
                task.run(id, currentJobExecution);
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
        }
    }

    /**
     * Performs a dry-run of the job.
     *
     * @return The result data created during job execution.
     */
    public DryRunJobResult dryRun() {
        log.debug("Dry-running job: {}", name);
        DryRunJobResult result = new DryRunJobResult();
        tasks.stream().forEach(task -> task.dryRun(result, id));
        log.debug("Finished dry-run of job: {}", name);
        return result;
    }

    /**
     * Cancels the job if it is currently running.
     */
    public void cancel() {
        if (currentJobExecution != null && JobExecutionState.RUNNING.equals(currentJobExecution.getExecutionState())) {
            currentJobExecution.setExecutionState(JobExecutionState.CANCELLED);
            currentJobExecution.setFinished(Instant.now());
        }
    }
}
