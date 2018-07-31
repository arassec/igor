package com.arassec.igor.core.model;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.concurrent.ConcurrencyGroup;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.core.model.provider.Provider;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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
    public void run(String jobId) {

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
            String concurrencyGroupId = String.format(CONCURRENCY_GROUP_ID_PATTERN, jobId, getName(), concurrencyLists.indexOf(concurrencyList));
            ConcurrencyGroup concurrencyGroup = new ConcurrencyGroup(concurrencyList, inputQueue, concurrencyGroupId);
            inputQueue = concurrencyGroup.getOutputQueue();
            concurrencyGroups.add(concurrencyGroup);
        }

        actions.stream().forEach(Action::initialize);

        // Read the data from the provider and start working:
        provider.initialize(jobId, name);
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
            action.complete();
        }
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
