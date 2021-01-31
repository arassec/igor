package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.annotation.validation.UniqueJobName;
import com.arassec.igor.core.model.job.concurrent.ConcurrencyGroup;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.job.misc.ProcessingFinishedCallback;
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
import java.util.*;
import java.util.concurrent.*;

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
     * Creates the meta-data part of a data item.
     *
     * @param jobId   The job's ID.
     * @param trigger The job's trigger.
     *
     * @return The meta-data for the job run.
     */
    public static Map<String, Object> createMetaData(String jobId, Trigger trigger) {
        Map<String, Object> metaData = new HashMap<>();
        metaData.put(DataKey.JOB_ID.getKey(), jobId);
        metaData.put(DataKey.TIMESTAMP.getKey(), Instant.now().toEpochMilli());
        Map<String, Object> triggerMetaData = trigger != null ? trigger.getMetaData() : Map.of();
        triggerMetaData.forEach(metaData::put);
        return metaData;
    }

    /**
     * Starts the job. Depending on the trigger type (scheduled vs event based) the job will either run once and will be finished
     * after the last data item has been processed, or it will remain in state {@link JobExecutionState#ACTIVE} until it is
     * manually stopped or igor is shut down.
     *
     * @param jobExecution The container for job execution information.
     */
    public void start(JobExecution jobExecution) {
        log.debug("Starting job: {} ({})", name, id);

        boolean processingFinishedCallbackSet = setProcessingFinishedCallbackIfApplicable();

        currentJobExecution = Objects.requireNonNullElseGet(jobExecution, JobExecution::new);
        if (trigger instanceof EventTrigger) {
            currentJobExecution.setExecutionState(JobExecutionState.ACTIVE);
        } else {
            currentJobExecution.setExecutionState(JobExecutionState.RUNNING);
        }
        currentJobExecution.setStarted(Instant.now());
        running = true;
        try {

            // Scan all actions to create lists of actions that belong to the same concurrency group (i.e. use the same
            // number of threads):
            List<List<Action>> concurrencyLists = createConcurrencyLists();

            // Create concurrency groups which start the threads that process the actions:
            BlockingQueue<Map<String, Object>> triggerInputQueue = new LinkedBlockingQueue<>();
            List<ConcurrencyGroup> concurrencyGroups = createConcurrencyGroups(concurrencyLists, triggerInputQueue, id,
                    jobExecution);

            if (trigger instanceof EventTrigger) {
                BlockingQueue<Map<String, Object>> triggerEventInputQueue = new LinkedBlockingQueue<>(1);
                if (!concurrencyLists.isEmpty() && !concurrencyLists.get(0).isEmpty()) {
                    triggerEventInputQueue = new LinkedBlockingQueue<>(concurrencyLists.get(0).get(0).getNumThreads());
                }
                ((EventTrigger) trigger).setEventQueue(triggerEventInputQueue);

                // Initialize IgorComponents used by the job:
                initialize(jobExecution);

                // Block until igor is shut down and wait for trigger events...
                while (JobExecutionState.ACTIVE.equals(currentJobExecution.getExecutionState())) {
                    Map<String, Object> triggerData = triggerEventInputQueue.poll(500, TimeUnit.MILLISECONDS);
                    if (triggerData != null) {
                        log.debug("Job '{}' ({}) triggered by event: {}", name, id, triggerData);
                        trigger.getData().forEach(triggerData::put); // A custom trigger might add additional data to the items.
                        dispatchInitialDataItem(triggerInputQueue, id, triggerData, processingFinishedCallbackSet);
                        currentJobExecution.setProcessedEvents(currentJobExecution.getProcessedEvents() + 1);
                    }
                }
            } else {
                // Initialize IgorComponents used by the job:
                initialize(jobExecution);

                // Read the data from the trigger and start working. This is used during simulated job runs to get the data from
                // an event based trigger, too!
                Map<String, Object> triggerData = Map.of();
                if (trigger != null) {
                    triggerData = trigger.getData();
                }
                dispatchInitialDataItem(triggerInputQueue, id, triggerData, processingFinishedCallbackSet);
            }

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
            shutdown(jobExecution);
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

            Object cancelLock = new Object();

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
     * Combines actions to a list that should be executed with the same number of threads. Keeps the order of the actions.
     *
     * @return {@link Action}s grouped by the number of threads they should execute with.
     */
    private List<List<Action>> createConcurrencyLists() {
        List<List<Action>> concurrencyLists = new LinkedList<>();

        // Initialized with -1 so that at least one concurrency-group is created.
        int lastNumThreads = -1;

        for (Action action : actions) {
            if (!action.isActive()) {
                continue;
            }
            if (action.getNumThreads() != lastNumThreads) {
                List<Action> concurrencyList = new LinkedList<>();
                concurrencyList.add(action);
                concurrencyLists.add(concurrencyList);
                lastNumThreads = action.getNumThreads();
            } else {
                concurrencyLists.get(concurrencyLists.size() - 1).add(action);
            }
        }

        return concurrencyLists;
    }

    /**
     * Creates concurrency groups, i.e. actions with the same amount of threads to process data and linked via queues.
     *
     * @param concurrencyLists  The ordered list of actions that should be executed with the same number of threads.
     * @param triggerInputQueue The initial input queue in which the trigger's data item will be put.
     * @param jobId             The job's ID.
     * @param jobExecution      The container for job execution data.
     *
     * @return List of {@link ConcurrencyGroup}s.
     */
    private List<ConcurrencyGroup> createConcurrencyGroups(List<List<Action>> concurrencyLists, BlockingQueue<Map<String,
            Object>> triggerInputQueue, String jobId, JobExecution jobExecution) {
        List<ConcurrencyGroup> concurrencyGroups = new LinkedList<>();
        BlockingQueue<Map<String, Object>> inputQueue = triggerInputQueue;

        for (List<Action> concurrencyList : concurrencyLists) {
            String concurrencyGroupId = String
                    .format(CONCURRENCY_GROUP_ID_PATTERN, jobId, getId(), concurrencyLists.indexOf(concurrencyList));
            ConcurrencyGroup concurrencyGroup = new ConcurrencyGroup(concurrencyList, inputQueue, concurrencyGroupId,
                    jobExecution);
            inputQueue = concurrencyGroup.getOutputQueue();
            concurrencyGroups.add(concurrencyGroup);
        }

        return concurrencyGroups;
    }

    /**
     * Initializes igor components before a job run.
     *
     * @param jobExecution Container for execution information.
     */
    private void initialize(JobExecution jobExecution) {
        if (trigger != null) {
            trigger.initialize(id, jobExecution);
            IgorComponentUtil.initializeConnectors(trigger, id, jobExecution);
        }
        if (!actions.isEmpty()) {
            actions.stream().filter(Action::isActive).forEach(action -> {
                action.initialize(id, jobExecution);
                IgorComponentUtil.initializeConnectors(action, id, jobExecution);
            });
        }
    }

    /**
     * Creates the initial data item and starts processing it with the first action.
     *
     * @param triggerInputQueue The input queue to put the trigger's data in.
     * @param jobId             The job's ID.
     * @param triggerData       Data provided by an event trigger.
     */
    private void dispatchInitialDataItem(BlockingQueue<Map<String, Object>> triggerInputQueue, String jobId, Map<String,
            Object> triggerData, boolean processingFinishedCallbackSet) {
        Map<String, Object> dataItem = new HashMap<>();
        dataItem.put(DataKey.META.getKey(), createMetaData(jobId, trigger));
        dataItem.put(DataKey.DATA.getKey(), triggerData);
        boolean added = false;
        while (!added) {
            try {
                added = triggerInputQueue.offer(dataItem, 100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        // This means we have to notify the trigger that the processing finished immediately, because there are no (active)
        // actions...
        if (this.getTrigger() instanceof ProcessingFinishedCallback && !processingFinishedCallbackSet) {
            ((ProcessingFinishedCallback) this.getTrigger()).processingFinished(dataItem);
        }
    }

    /**
     * Awaits thread termination of each concurrency group.
     *
     * @param concurrencyGroups List of concurrency group to wait for.
     */
    private void awaitThreadTermination(List<ConcurrencyGroup> concurrencyGroups) {
        boolean allThreadsTerminated = false;
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
                IgorComponentUtil.shutdownConnectors(action, id, jobExecution);
                action.shutdown(id, jobExecution);
            });
        }
        if (trigger != null) {
            IgorComponentUtil.shutdownConnectors(trigger, id, jobExecution);
            trigger.shutdown(id, jobExecution);
        }
    }

    /**
     * Sets the trigger as 'processing finished' callback to the last action of the job, if the trigger is appropriate and actions
     * exist.
     *
     * @return {@code true} if the callback has been set, {@code false} otherwise.
     */
    private boolean setProcessingFinishedCallbackIfApplicable() {
        if (this.getTrigger() instanceof ProcessingFinishedCallback && !actions.isEmpty()) {
            for (int i = (actions.size() - 1); i >= 0; i--) {
                if (actions.get(i).isActive()) {
                    actions.get(i).setProcessingFinishedCallback((ProcessingFinishedCallback) this.getTrigger());
                    return true;
                }
            }
        }
        return false;
    }

}
