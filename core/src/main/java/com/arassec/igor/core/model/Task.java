package com.arassec.igor.core.model;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.concurrent.ConcurrencyGroup;
import com.arassec.igor.core.model.dryrun.DryRunActionResult;
import com.arassec.igor.core.model.dryrun.DryRunJobResult;
import com.arassec.igor.core.model.dryrun.DryRunTaskResult;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.core.model.provider.Provider;
import com.rits.cloning.Cloner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

/**
 * A task is an encapsulated unit of work in a job. It is responsible for processing actions, either in the main thread of the
 * task or as multiple threads in their own thread pool.
 */
public class Task {

    /**
     * Defines
     */
    private static final int NUM_THREADS_INITIAL = -2;

    private static final String CONCURRENCY_GROUP_ID_PATTERN = "%s-%s-%d";

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
     * Runs the task.
     */
    public void run(String jobName) {

        // Scan all actions to create lists of actions that belong to the same concurrency group (i.e. use the same number of
        // threads):
        List<List<Action>> concurrencyLists = new LinkedList<>();

        int lastNumThreads = NUM_THREADS_INITIAL;

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
            ConcurrencyGroup concurrencyGroup = new ConcurrencyGroup(concurrencyList, inputQueue, concurrencyGroupId);
            inputQueue = concurrencyGroup.getOutputQueue();
            concurrencyGroups.add(concurrencyGroup);
        }

        actions.stream().forEach(Action::initialize);

        // Read the data from the provider and start working:
        provider.initialize(jobName, name);
        while (provider.hasNext()) {
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

        for (ConcurrencyGroup concurrencyGroup : concurrencyGroups) {
            concurrencyGroup.shutdown();
        }

        for (Action action : actions) {
            action.complete(jobName, name);
        }
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public List<Action> getActions() {
        return actions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
