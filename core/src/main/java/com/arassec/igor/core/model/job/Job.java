package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.annotation.validation.UniqueJobName;
import com.arassec.igor.core.model.job.concurrent.ConcurrencyGroup;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.job.starter.DefaultJobStarter;
import com.arassec.igor.core.model.job.starter.EventTriggeredJobStarter;
import com.arassec.igor.core.model.job.starter.JobStarter;
import com.arassec.igor.core.model.trigger.EventTrigger;
import com.arassec.igor.core.model.trigger.Trigger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
     * The name pattern for concurrency-group IDs.
     */
    private static final String CONCURRENCY_GROUP_ID_PATTERN = "%s_%s_%d";

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
    @Size(max = 500)
    private String description;

    /**
     * Enables or disables the job.
     */
    private boolean active;

    /**
     * Max. number of job-execution entries to keep for this job.
     */
    @Builder.Default
    @Positive
    private int historyLimit = 5;

    /**
     * Limits the data items during simulated job executions.
     */
    @Builder.Default
    @Positive
    private int simulationLimit = 25;

    /**
     * The number of threads, actions are executed with.
     */
    @Builder.Default
    @Positive
    private int numThreads = 1;

    /**
     * A trigger for the job.
     */
    @Valid
    private Trigger trigger;

    /**
     * The actions this job will perform during its run.
     */
    @Builder.Default
    @Valid
    private List<Action> actions = new LinkedList<>();

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
     * Starts the job. Depending on the trigger type (scheduled vs event based) the job will either run once and will be finished
     * after the last data item has been processed, or it will remain in state {@link JobExecutionState#ACTIVE} until it is
     * manually stopped or igor is shut down.
     *
     * @param jobExecution The container for job execution information.
     */
    public void start(JobExecution jobExecution) {
        log.debug("Starting job: {} ({})", name, id);

        currentJobExecution = Objects.requireNonNullElseGet(jobExecution, () -> JobExecution.builder().build());
        if (trigger instanceof EventTrigger) {
            currentJobExecution.setExecutionState(JobExecutionState.ACTIVE);
        } else {
            currentJobExecution.setExecutionState(JobExecutionState.RUNNING);
        }
        currentJobExecution.setStarted(Instant.now());
        currentJobExecution.setJobId(id);

        running = true;
        try {
            // Starts processing data items:
            JobStarter jobStarter;
            if (trigger instanceof EventTrigger) {
                jobStarter = new EventTriggeredJobStarter(trigger, actions, currentJobExecution, numThreads);
            } else {
                jobStarter = new DefaultJobStarter(trigger, actions, currentJobExecution, numThreads);
            }
            List<ConcurrencyGroup> concurrencyGroups = jobStarter.process();

            // Completes each action inside each concurrency group:
            concurrencyGroups.forEach(ConcurrencyGroup::complete);

            // Shuts the concurrency group down:
            concurrencyGroups.forEach(ConcurrencyGroup::shutdown);

            // Awaits thread termination of each concurrency group:
            awaitThreadTermination(concurrencyGroups);

            if (currentJobExecution.isRunningOrActive()) {
                currentJobExecution.setExecutionState(JobExecutionState.FINISHED);
            }
        } catch (Exception e) {
            log.error("Exception during job execution!", e);
            currentJobExecution.fail(e);
        } finally {
            shutdown(currentJobExecution);
            currentJobExecution.setFinished(Instant.now());
            running = false;
            log.debug("Finished job: {} ({}): {}", name, id, currentJobExecution);
        }
    }

    /**
     * Cancels the job if it is currently running. This may take some time until all actions finished their work.
     */
    public void cancel() {
        if (currentJobExecution != null) {
            if (JobExecutionState.RUNNING.equals(currentJobExecution.getExecutionState())) {
                currentJobExecution.setExecutionState(JobExecutionState.CANCELLED);
            } else if (JobExecutionState.ACTIVE.equals(currentJobExecution.getExecutionState())) {
                currentJobExecution.setExecutionState(JobExecutionState.FINISHED);
            }

            var cancelLock = new Object();

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(() -> {
                synchronized (cancelLock) {
                    log.trace("Checking if job {} is still running: {}", getId(), getName());
                    if (!isRunning()) {
                        cancelLock.notifyAll();
                    }
                }
            }, 0, 100, TimeUnit.MILLISECONDS);

            synchronized (cancelLock) {
                try {
                    while (isRunning()) {
                        cancelLock.wait();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            executor.shutdown();
        }
    }

    /**
     * Awaits thread termination of each concurrency group.
     *
     * @param concurrencyGroups List of concurrency group to wait for.
     */
    private void awaitThreadTermination(List<ConcurrencyGroup> concurrencyGroups) {
        var allThreadsTerminated = false;
        while (!allThreadsTerminated) {
            allThreadsTerminated = true;
            for (ConcurrencyGroup concurrencyGroup : concurrencyGroups) {
                allThreadsTerminated = (allThreadsTerminated && concurrencyGroup.awaitTermination());
            }
            log.debug("Threads terminated over all concurrency-groups: {}", allThreadsTerminated);
        }
    }

    /**
     * Shuts igor components down after a job run.
     *
     * @param jobExecution Container for execution information.
     */
    private void shutdown(JobExecution jobExecution) {
        if (!actions.isEmpty()) {
            actions.stream().filter(Action::isActive).forEach(action -> {
                IgorConnectorUtil.shutdownConnectors(action, jobExecution);
                action.shutdown(jobExecution);
            });
        }
        if (trigger != null) {
            IgorConnectorUtil.shutdownConnectors(trigger, jobExecution);
            trigger.shutdown(jobExecution);
        }
    }

}
