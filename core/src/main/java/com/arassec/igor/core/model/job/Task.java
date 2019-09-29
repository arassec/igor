package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.concurrent.ConcurrencyGroup;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.provider.Provider;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * A task is an encapsulated unit of work in a job. It is responsible for processing actions, either in the main thread of the
 * task or as multiple threads in their own thread pool.
 */
@Data
@Slf4j
public class Task {

    /**
     * The key for the meta-data.
     */
    public static final String META_KEY = "meta";

    /**
     * The key for the provider's data.
     */
    public static final String DATA_KEY = "data";

    /**
     * The key for the simulation property.
     */
    public static final String SIMULATION_KEY = "simulation";

    /**
     * The task's (UU)ID.
     */
    private String id;

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
     * Enables or disables the complete task.
     */
    private boolean active;

    /**
     * The data provider for the task.
     */
    private Provider provider;

    /**
     * The actions this job will perform during its run.
     */
    private List<Action> actions = new LinkedList<>();

    /**
     * Creates a new Task with a random ID.
     */
    public Task() {
        id = UUID.randomUUID().toString();
    }

    /**
     * Creates a new Task.
     *
     * @param id The task's ID.
     */
    public Task(String id) {
        this.id = id;
    }

    /**
     * Runs the task by first creating concurrency-groups for actions that should run with the same amount of threads and then
     * requesting the provider for data to process.
     * <p>
     * The retrieved data is than processed by the configured actions in ther concurrency-groups.
     *
     * @param jobId        The ID of the job.
     * @param jobExecution The {@link JobExecution} that contains the state of the current job run.
     */
    public void run(Long jobId, JobExecution jobExecution) {

        log.debug("Starting task '{}'", name);

        jobExecution.setCurrentTask(name);

        // Scan all actions to create lists of actions that belong to the same concurrency group (i.e. use the same
        // number of threads):
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

        // Create concurrency groups which start the threads that process the actions:
        List<ConcurrencyGroup> concurrencyGroups = new LinkedList<>();
        BlockingQueue<Map<String, Object>> providerInputQueue = new ArrayBlockingQueue<>(1000);
        BlockingQueue<Map<String, Object>> inputQueue = providerInputQueue;

        for (List<Action> concurrencyList : concurrencyLists) {
            String concurrencyGroupId = String
                    .format(CONCURRENCY_GROUP_ID_PATTERN, jobId, getId(), concurrencyLists.indexOf(concurrencyList));
            ConcurrencyGroup concurrencyGroup = new ConcurrencyGroup(concurrencyList, inputQueue, concurrencyGroupId,
                    jobExecution);
            inputQueue = concurrencyGroup.getOutputQueue();
            concurrencyGroups.add(concurrencyGroup);
        }

        actions.forEach(Action::initialize);


        // Read the data from the provider and start working:
        provider.initialize(jobId, id, jobExecution);
        while (provider.hasNext() && jobExecution.isRunning()) {
            Map<String, Object> data = new HashMap<>();
            data.put(META_KEY, createMetaData(jobId, id));
            data.put(DATA_KEY, provider.next());
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

        // Completes each action inside each concurrency group:
        concurrencyGroups.forEach(ConcurrencyGroup::complete);

        // Shuts the concurrency group down and awaits thread termination:
        concurrencyGroups.forEach(ConcurrencyGroup::shutdown);

        boolean allThreadsTerminated = false;
        while (!allThreadsTerminated) {
            allThreadsTerminated = true;
            for (ConcurrencyGroup concurrencyGroup : concurrencyGroups) {
                allThreadsTerminated = (allThreadsTerminated && concurrencyGroup.awaitTermination());
            }
            log.debug("Threads terminated over all concurrency-groups: {}", allThreadsTerminated);
        }

        actions.forEach(action -> action.shutdown(jobId, id));

        log.debug("Task '{}' finished!", name);
    }

    /**
     * Creates the meta-data.
     *
     * @param jobId The job's ID.
     *
     * @return The meta-data for the job run.
     */
    public static Object createMetaData(Long jobId, String taskId) {
        Map<String, Object> metaData = new HashMap<>();
        metaData.put("jobId", jobId);
        metaData.put("taskId", taskId);
        metaData.put("timestamp", Instant.now().toEpochMilli());
        return metaData;
    }

}
