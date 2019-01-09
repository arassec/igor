package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.concurrent.ConcurrencyGroup;
import com.arassec.igor.core.model.job.dryrun.DryRunActionResult;
import com.arassec.igor.core.model.job.dryrun.DryRunJobResult;
import com.arassec.igor.core.model.job.dryrun.DryRunTaskResult;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.core.model.provider.Provider;
import com.rits.cloning.Cloner;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

/**
 * A task is an encapsulated unit of work in a job. It is responsible for processing actions, either in the main thread of the
 * task or as multiple threads in their own thread pool.
 */
@Data
@Slf4j
public class Task {

    /**
     * The name pattern for concurrency-group IDs.
     */
    private static final String CONCURRENCY_GROUP_ID_PATTERN = "%s_%s_%d";

    /**
     * The name of the task.
     */
    private String name;

    /**
     * The task's description.
     */
    private String description;

    /**
     * The data provider for the task.
     */
    private Provider provider;

    /**
     * The actions this job will perform during its run.
     */
    private List<Action> actions = new LinkedList<>();

    /**
     * Runs the task by first creating concurrency-groups for actions that should run with the same amount of threads
     * and then requesting the provider for data to process.
     * <p>
     * The retrieved data is than processed by the configured actions in ther concurrency-groups.
     *
     * @param jobName      The name of the job.
     * @param jobExecution The {@link JobExecution} that contains the state of the current job run.
     */
    public void run(String jobName, JobExecution jobExecution) {

        log.debug("Starting task '{}'", name);

        jobExecution.setCurrentTask(name);

        // Scan all actions to create lists of actions that belong to the same concurrency group (i.e. use the same
        // number of threads):
        List<List<Action>> concurrencyLists = new LinkedList<>();

        // Initialized with -1 so that at least one concurrency-group is created.
        int lastNumThreads = -1;

        for (Action action : actions) {
            if (action.getNumThreads() != lastNumThreads) {
                List<Action> concurrencyList = new LinkedList<>();
                concurrencyList.add(action);
                concurrencyLists.add(concurrencyList);
                lastNumThreads = action.getNumThreads();
            } else {
                concurrencyLists.get(concurrencyLists.size() - 1).add(action);
            }
        }

        // Create concurrency groups which start the threads that process the actions:
        List<ConcurrencyGroup> concurrencyGroups = new LinkedList<>();
        BlockingQueue<IgorData> providerInputQueue = new ArrayBlockingQueue<>(1000);
        BlockingQueue<IgorData> inputQueue = providerInputQueue;

        for (List<Action> concurrencyList : concurrencyLists) {
            String concurrencyGroupId = String.format(CONCURRENCY_GROUP_ID_PATTERN, jobName, getName(), concurrencyLists.indexOf(concurrencyList));
            ConcurrencyGroup concurrencyGroup = new ConcurrencyGroup(concurrencyList, inputQueue, concurrencyGroupId, jobExecution);
            inputQueue = concurrencyGroup.getOutputQueue();
            concurrencyGroups.add(concurrencyGroup);
        }

        actions.stream().forEach(Action::initialize);

        // Read the data from the provider and start working:
        provider.initialize(jobName, name);
        while (provider.hasNext() && jobExecution.isRunning()) {
            IgorData data = provider.next();
            boolean added = false;
            while (!added) {
                added = providerInputQueue.offer(data);
                if (!added) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // Doesn't matter, we just wanted to wait before the next offer()...
                    }
                }
            }
        }

        concurrencyGroups.stream().forEach(ConcurrencyGroup::shutdown);

        boolean allThreadsTerminated = false;
        while (!allThreadsTerminated) {
            allThreadsTerminated = true;
            for (ConcurrencyGroup concurrencyGroup : concurrencyGroups) {
                allThreadsTerminated = (allThreadsTerminated && concurrencyGroup.awaitTermination());
            }
            log.debug("Threads terminated over all concurrency-groups: {}", allThreadsTerminated);
        }

        actions.stream().forEach(action -> action.complete(jobName, name));

        log.debug("Task '{}' finished!", name);
    }

    /**
     * Performs a dry-run of the task collecting data.
     *
     * @param result  The target object to store results in.
     * @param jobName The name of the job currently executing.
     */
    public void dryRun(DryRunJobResult result, String jobName) {
        provider.initialize(jobName, name);
        DryRunTaskResult taskResult = new DryRunTaskResult();

        Cloner cloner = new Cloner();

        List<IgorData> providerResult = new LinkedList<>();
        while (provider.hasNext()) {
            IgorData igorData = provider.next();
            providerResult.add(igorData);
            taskResult.getProviderResults().add(cloner.deepClone(igorData));
        }

        List<IgorData> actionData = providerResult;
        for (Action action : actions) {
            DryRunActionResult actionResult = new DryRunActionResult();
            actionData = actionData.stream().filter(igorData -> action.dryRun(igorData)).collect(Collectors.toList());
            actionData.stream().forEach(igorData -> actionResult.getResults().add(cloner.deepClone(igorData)));
            taskResult.getActionResults().add(actionResult);
        }

        result.getTaskResults().add(taskResult);
    }

}