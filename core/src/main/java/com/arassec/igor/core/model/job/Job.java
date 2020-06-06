package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.util.validation.UniqueJobName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Defines an igor job.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@UniqueJobName
public class Job {

    /**
     * The job's ID.
     */
    @NotEmpty
    private String id;

    /**
     * The job's name.
     */
    @NotEmpty
    @Size(max = 250)
    private String name;

    /**
     * A job description.
     */
    private String description;

    /**
     * A trigger for the job.
     */
    @Valid
    private Trigger trigger;

    /**
     * Enables or disables the job.
     */
    private boolean active;

    /**
     * Max. number of job-execution entries to keep for this job.
     */
    @Builder.Default
    @PositiveOrZero
    private int historyLimit = 5;

    /**
     * The tasks this job will perform.
     */
    @Builder.Default
    @Valid
    private List<Task> tasks = new LinkedList<>();

    /**
     * Contains information about a job run.
     */
    private JobExecution currentJobExecution;

    /**
     * Indicates whether the job is running or not.
     */
    @Builder.Default
    private boolean running = false;

    /**
     * Indicates whether a successful job run compensates previous failures or not.
     */
    @Builder.Default
    private boolean faultTolerant = true;

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
            initialize(jobExecution);
            for (Task task : tasks) {
                if (!currentJobExecution.isRunning()) {
                    break;
                }
                currentJobExecution.setCurrentTask(task.getName());
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
            shutdown(jobExecution);
            currentJobExecution.setFinished(Instant.now());
            running = false;
            log.debug("Finished job: {} ({}): {}", name, id, currentJobExecution);
        }
    }

    /**
     * Cancels the job if it is currently running. The job might not stop immediately if it is executing a non-interruptible task.
     * Check {@link Job#isRunning()} regularly to see when the job execution finished.
     */
    public void cancel() {
        if (currentJobExecution != null && JobExecutionState.RUNNING.equals(currentJobExecution.getExecutionState())) {
            currentJobExecution.setExecutionState(JobExecutionState.CANCELLED);
        }
    }

    /**
     * Initializes igor components before a job run.
     *
     * @param jobExecution Container for execution information.
     */
    private void initialize(JobExecution jobExecution) {
        if (trigger != null) {
            trigger.initialize(id, null, jobExecution);
            IgorComponentUtil.initializeConnectors(trigger, id, null, jobExecution);
        }
    }

    /**
     * Shuts igor components down after a job run.
     *
     * @param jobExecution Container for execution information.
     */
    private void shutdown(JobExecution jobExecution) {
        if (trigger != null) {
            IgorComponentUtil.shutdownConnectors(trigger, id, null, jobExecution);
            trigger.shutdown(id, null, jobExecution);
        }
    }

}
