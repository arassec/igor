package com.arassec.igor.core.model.job.starter;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.IgorConnectorUtil;
import com.arassec.igor.core.model.job.concurrent.ConcurrencyGroup;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ProcessingFinishedCallback;
import com.arassec.igor.core.model.trigger.Trigger;
import lombok.Data;

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
     * @param numThreads   The number of threads the job should execute with.
     */
    public DefaultJobStarter(Trigger trigger, List<Action> actions, JobExecution jobExecution, int numThreads) {
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
        this.concurrencyLists = createConcurrencyLists(numThreads);
        this.concurrencyGroups = createConcurrencyGroups(concurrencyLists, initialInputQueue, jobExecution, numThreads);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConcurrencyGroup> process() {
        // Initialize the trigger and actions:
        initialize(jobExecution);

        // Read the data from the trigger and start working.
        Map<String, Object> dataItem = Map.of();
        if (trigger != null) {
            dataItem = trigger.createDataItem();
        }

        dispatchInitialDataItem(initialInputQueue, dataItem, processingFinishedCallbackSet);

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
            IgorConnectorUtil.initializeConnectors(trigger, jobExecution);
        }
        if (!actions.isEmpty()) {
            actions.stream().filter(Action::isActive).forEach(action -> {
                action.initialize(jobExecution);
                IgorConnectorUtil.initializeConnectors(action, jobExecution);
            });
        }
    }

    /**
     * Creates the initial data item and starts processing it with the first action.
     *
     * @param inputQueue                    The input queue to put the first data item in.
     * @param initialDataItem               Data item provided by the trigger.
     * @param processingFinishedCallbackSet Indicates whether "processing finished" callbacks must be called or not.
     */
    protected void dispatchInitialDataItem(BlockingQueue<Map<String, Object>> inputQueue, Map<String,
        Object> initialDataItem, boolean processingFinishedCallbackSet) {
        var added = false;
        while (!added) {
            try {
                added = inputQueue.offer(initialDataItem, 100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        // This means we have to notify the trigger that the processing finished immediately, because there are no (active)
        // actions...
        if (trigger instanceof ProcessingFinishedCallback processingFinishedCallback && !processingFinishedCallbackSet) {
            processingFinishedCallback.processingFinished(initialDataItem);
        }
    }

    /**
     * Scan all actions to create lists of actions that belong to the same concurrency group (i.e. use the same number of
     * threads).
     * <p>
     * Keeps the order of the actions.
     *
     * @param numThreads The number of threads the job's actions should execute with.
     *
     * @return {@link Action}s grouped by the number of threads they should execute with.
     */
    private List<List<Action>> createConcurrencyLists(int numThreads) {
        List<List<Action>> result = new LinkedList<>();

        // Initialized with -1 so that at least one concurrency-group is created.
        int lastNumThreads = -1;

        for (Action action : actions) {
            if (!action.isActive()) {
                continue;
            }
            int threads = action.enforceSingleThread() ? 1 : numThreads;
            if (threads != lastNumThreads) {
                List<Action> concurrencyList = new LinkedList<>();
                concurrencyList.add(action);
                result.add(concurrencyList);
                lastNumThreads = threads;
            } else {
                result.getLast().add(action);
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
     * @param numThreads       The number of threads the job's actions should execute with.
     *
     * @return List of {@link ConcurrencyGroup}s.
     */
    private List<ConcurrencyGroup> createConcurrencyGroups(List<List<Action>> concurrencyLists, BlockingQueue<Map<String,
        Object>> inputQueue, JobExecution jobExecution, int numThreads) {
        List<ConcurrencyGroup> result = new LinkedList<>();
        BlockingQueue<Map<String, Object>> inputQueueHolder = inputQueue;

        for (List<Action> concurrencyList : concurrencyLists) {
            var concurrencyGroupId = String
                .format(CONCURRENCY_GROUP_ID_PATTERN, jobExecution.getJobId(), concurrencyLists.indexOf(concurrencyList));
            var concurrencyGroup = new ConcurrencyGroup(concurrencyList, inputQueueHolder, concurrencyGroupId,
                jobExecution, numThreads);
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
        if (trigger instanceof ProcessingFinishedCallback processingFinishedCallback && !actions.isEmpty()) {
            for (int i = (actions.size() - 1); i >= 0; i--) {
                if (actions.get(i).isActive()) {
                    actions.get(i).setProcessingFinishedCallback(processingFinishedCallback);
                    return true;
                }
            }
        }
        return false;
    }

}
