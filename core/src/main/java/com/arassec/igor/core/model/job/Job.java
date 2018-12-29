package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.job.dryrun.DryRunJobResult;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

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
     * The tasks this job will perform.
     */
    private List<Task> tasks = new LinkedList<>();

    /**
     * Contains information about a job run.
     */
    private JobExecution jobExecution;

    /**
     * A listener that is notified when the job finishes.
     */
    private JobListener jobListener;

    /**
     * Runs the job.
     */
    public void run() {
        log.debug("Running job: {} ({})", name, id);
        jobListener.notifyStarted(this);
        jobExecution = new JobExecution();
        jobExecution.setStarted(Instant.now());
        jobExecution.setExecutionState(JobExecutionState.RUNNING);
        try {
            for (Task task : tasks) {
                if (!jobExecution.isRunning()) {
                    break;
                }
                task.run(name, jobExecution);
            }
            if (jobExecution.isRunning()) {
                jobExecution.setExecutionState(JobExecutionState.FINISHED);
            }
        } catch (Exception e) {
            log.error("Exception during job execution!", e);
            jobExecution.setExecutionState(JobExecutionState.FAILED);
        } finally {
            jobExecution.setFinished(Instant.now());
            jobListener.notifyFinished(this);
            log.debug("Finished job: {} ({}): {}", name, id, jobExecution);
        }
    }

    /**
     * Performs a dry-run of the job.
     *
     * @return The result data created during job execution.
     */
    public DryRunJobResult dryRun() {
        log.debug("Dry-isRunning job: {}", name);
        DryRunJobResult result = new DryRunJobResult();
        tasks.stream().forEach(task -> task.dryRun(result, name));
        log.debug("Finished dry-run of job: {}", name);
        return result;
    }

    /**
     * Cancels the job if it is currently isRunning.
     */
    public void cancel() {
        if (jobExecution != null && JobExecutionState.RUNNING.equals(jobExecution.getExecutionState())) {
            jobExecution.setExecutionState(JobExecutionState.CANCELLED);
            jobExecution.setFinished(Instant.now());
        }
    }
}
