package com.arassec.igor.core.model.job.starter;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.IgorComponentUtil;
import com.arassec.igor.core.model.job.concurrent.ConcurrencyGroup;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ProcessingFinishedCallback;
import com.arassec.igor.core.model.trigger.Trigger;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Default job starter that creates an initial, blank data item and thus starts the job.
 */
@Data
public class DefaultJobStarter implements JobStarter {

    /**
     * The name pattern for concurrency-group IDs.
     */
    private static final String CONCURRENCY_GROUP_ID_PATTERN = "%s_%d";

    /**
     * The job's trigger.
     */
    protected final Trigger trigger;

    /**
     * The job's actions.
     */
    protected final List<Action> actions;

    /**
     * The current {@link JobExecution}.
     */
    protected final JobExecution jobExecution;

    /**
     * Is set to {@code true}, if there is a {@link ProcessingFinishedCallback} configured in an action that must be called after
     * a data item is processed.
     */
    protected boolean processingFinishedCallbackSet;

    /**
     * List of actions in the user-defined order, that should run with the same amount of threads.
     */
    protected List<List<Action>> concurrencyLists;

    /**
     * List of {@link ConcurrencyGroup}s of the job's actions.
     */
    protected List<ConcurrencyGroup> concurrencyGroups;

    /**
     * The input queue for the initial data item that is then processed by the actions.
     */
    protected BlockingQueue<Map<String, Object>> initialInputQueue;

    /**
     * Creates a new instance.
     *
     * @param trigger      The job's trigger.
     * @param actions      The job's actions.
     * @param jobExecution The current job execution.
     */
    public DefaultJobStarter(Trigger trigger, List<Action> actions, JobExecution jobExecution) {
        if (actions == null) {
            throw new IllegalArgumentException("Actions must not be null!");
        }
        if (jobExecution == null) {
            throw new IllegalArgumentException("JobExecution must not be null!");
        }
        this.trigger = trigger;
        this.actions = actions;
        this.jobExecution = jobExecution;
        this.processingFinishedCallbackSet = setProcessingFinishedCallbackIfApplicable();
        this.initialInputQueue = new LinkedBlockingQueue<>();
        this.concurrencyLists = createConcurrencyLists();
        this.concurrencyGroups = createConcurrencyGroups(concurrencyLists, initialInputQueue, jobExecution);
    }

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
     * {@inheritDoc}
     */
    @Override
    public List<ConcurrencyGroup> process() {
        // Initialize the trigger and actions:
        initialize(jobExecution);

        // Read the data from the trigger and start working.
        Map<String, Object> triggerData = Map.of();
        if (trigger != null) {
            triggerData = trigger.getData();
        }

        dispatchInitialDataItem(initialInputQueue, jobExecution.getJobId(), triggerData, processingFinishedCallbackSet);

        return concurrencyGroups;
    }

    /**
     * Initializes igor components before a job run.
     *
     * @param jobExecution Container for execution information.
     */
    protected void initialize(JobExecution jobExecution) {
        if (trigger != null) {
            trigger.initialize(jobExecution);
            IgorComponentUtil.initializeConnectors(trigger, jobExecution);
        }
        if (!actions.isEmpty()) {
            actions.stream().filter(Action::isActive).forEach(action -> {
                action.initialize(jobExecution);
                IgorComponentUtil.initializeConnectors(action, jobExecution);
            });
        }
    }

    /**
     * Creates the initial data item and starts processing it with the first action.
     *
     * @param inputQueue                    The input queue to put the first data item in.
     * @param jobId                         The job's ID.
     * @param triggerData                   Data provided by an event trigger.
     * @param processingFinishedCallbackSet Indicates whether "processing finished" callbacks must be called or not.
     */
    protected void dispatchInitialDataItem(BlockingQueue<Map<String, Object>> inputQueue, String jobId, Map<String,
            Object> triggerData, boolean processingFinishedCallbackSet) {
        Map<String, Object> dataItem = new HashMap<>();
        dataItem.put(DataKey.META.getKey(), DefaultJobStarter.createMetaData(jobId, trigger));
        dataItem.put(DataKey.DATA.getKey(), triggerData);
        boolean added = false;
        while (!added) {
            try {
                added = inputQueue.offer(dataItem, 100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        // This means we have to notify the trigger that the processing finished immediately, because there are no (active)
        // actions...
        if (trigger instanceof ProcessingFinishedCallback && !processingFinishedCallbackSet) {
            ((ProcessingFinishedCallback) trigger).processingFinished(dataItem);
        }
    }

    /**
     * Scan all actions to create lists of actions that belong to the same concurrency group (i.e. use the same number of
     * threads).
     * <p>
     * Keeps the order of the actions.
     *
     * @return {@link Action}s grouped by the number of threads they should execute with.
     */
    private List<List<Action>> createConcurrencyLists() {
        List<List<Action>> result = new LinkedList<>();

        // Initialized with -1 so that at least one concurrency-group is created.
        int lastNumThreads = -1;

        for (Action action : actions) {
            if (!action.isActive()) {
                continue;
            }
            if (action.getNumThreads() != lastNumThreads) {
                List<Action> concurrencyList = new LinkedList<>();
                concurrencyList.add(action);
                result.add(concurrencyList);
                lastNumThreads = action.getNumThreads();
            } else {
                result.get(result.size() - 1).add(action);
            }
        }

        return result;
    }

    /**
     * Creates concurrency groups, i.e. actions with the same amount of threads to process data and linked via queues.
     *
     * @param concurrencyLists The ordered list of actions that should be executed with the same number of threads.
     * @param inputQueue       The initial input queue in which the first data item will be put.
     * @param jobExecution     The container for job execution data.
     *
     * @return List of {@link ConcurrencyGroup}s.
     */
    private List<ConcurrencyGroup> createConcurrencyGroups(List<List<Action>> concurrencyLists, BlockingQueue<Map<String,
            Object>> inputQueue, JobExecution jobExecution) {
        List<ConcurrencyGroup> result = new LinkedList<>();
        BlockingQueue<Map<String, Object>> inputQueueHolder = inputQueue;

        for (List<Action> concurrencyList : concurrencyLists) {
            String concurrencyGroupId = String
                    .format(CONCURRENCY_GROUP_ID_PATTERN, jobExecution.getJobId(), concurrencyLists.indexOf(concurrencyList));
            ConcurrencyGroup concurrencyGroup = new ConcurrencyGroup(concurrencyList, inputQueueHolder, concurrencyGroupId,
                    jobExecution);
            inputQueueHolder = concurrencyGroup.getOutputQueue();
            result.add(concurrencyGroup);
        }

        return result;
    }

    /**
     * Sets the trigger as 'processing finished' callback to the last (active) action of the job, if the trigger is appropriate
     * and actions exist.
     *
     * @return {@code true} if the callback has been set, {@code false} otherwise.
     */
    private boolean setProcessingFinishedCallbackIfApplicable() {
        if (trigger instanceof ProcessingFinishedCallback && !actions.isEmpty()) {
            for (int i = (actions.size() - 1); i >= 0; i--) {
                if (actions.get(i).isActive()) {
                    actions.get(i).setProcessingFinishedCallback((ProcessingFinishedCallback) trigger);
                    return true;
                }
            }
        }
        return false;
    }

}
