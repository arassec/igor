package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.concurrent.ConcurrencyGroup;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.provider.Provider;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
     * The task's (UU)ID.
     */
    private String id;

    /**
     * The name of the task.
     */
    @NotEmpty
    @Size(max = 250)
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
    @Valid
    private Provider provider;

    /**
     * The actions this job will perform during its run.
     */
    @Valid
    private List<Action> actions = new LinkedList<>();

    /**
     * Creates the meta-data.
     *
     * @param jobId The job's ID.
     *
     * @return The meta-data for the job run.
     */
    public static Map<String, Object> createMetaData(String jobId, String taskId) {
        Map<String, Object> metaData = new HashMap<>();
        metaData.put(DataKey.JOB_ID.getKey(), jobId);
        metaData.put(DataKey.TASK_ID.getKey(), taskId);
        metaData.put(DataKey.TIMESTAMP.getKey(), Instant.now().toEpochMilli());
        return metaData;
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
    public void run(String jobId, JobExecution jobExecution) {

        log.debug("Starting task '{}'", name);

        jobExecution.setCurrentTask(name);

        if (provider == null) {
            log.debug("No provider set for task '{}'", name);
            return;
        }

        // Scan all actions to create lists of actions that belong to the same concurrency group (i.e. use the same
        // number of threads):
        List<List<Action>> concurrencyLists = createConcurrencyLists();

        // Create concurrency groups which start the threads that process the actions:
        BlockingQueue<Map<String, Object>> providerInputQueue = new LinkedBlockingQueue<>();
        List<ConcurrencyGroup> concurrencyGroups = createConcurrencyGroups(concurrencyLists, providerInputQueue, jobId,
                jobExecution);

        // Initialize IgorComponents used by the task:
        initialize(jobId, jobExecution);

        // Read the data from the provider and start working:
        processProviderData(providerInputQueue, jobId, jobExecution);

        // Completes each action inside each concurrency group:
        concurrencyGroups.forEach(ConcurrencyGroup::complete);

        // Shuts the concurrency group down:
        concurrencyGroups.forEach(ConcurrencyGroup::shutdown);

        // Awaits thread termination of each concurrency group:
        awaitThreadTermination(concurrencyGroups);

        // Shutdown IgorComponents after task execution:
        shutdown(jobId, jobExecution);

        log.debug("Task '{}' finished!", name);
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
     * @param concurrencyLists   The ordered list of actions that should be executed with the same number of threads.
     * @param providerInputQueue The initial input queue in which the provider's data will be put by the task.
     * @param jobId              The job's ID.
     * @param jobExecution       The container for job execution data.
     *
     * @return List of {@link ConcurrencyGroup}s.
     */
    private List<ConcurrencyGroup> createConcurrencyGroups(List<List<Action>> concurrencyLists, BlockingQueue<Map<String,
            Object>> providerInputQueue, String jobId, JobExecution jobExecution) {
        List<ConcurrencyGroup> concurrencyGroups = new LinkedList<>();
        BlockingQueue<Map<String, Object>> inputQueue = providerInputQueue;

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
     * Initializes the {@link Provider} and all {@link Action}s of the task.
     *
     * @param jobId        The job's ID.
     * @param jobExecution Contains information about the job execution.
     */
    private void initialize(String jobId, JobExecution jobExecution) {
        provider.initialize(jobId, id, jobExecution);
        IgorComponentUtil.initializeServices(provider, jobId, id, jobExecution);
        actions.stream().filter(Action::isActive).forEach(action -> {
            action.initialize(jobId, id, jobExecution);
            IgorComponentUtil.initializeServices(action, jobId, id, jobExecution);
        });
    }

    /**
     * Processes the data offered by the provider and gets the actions to work.
     *
     * @param providerInputQueue The input queue to put the provider's data in.
     * @param jobId              The job's ID.
     * @param jobExecution       Contains information about the job execution.
     */
    private void processProviderData(BlockingQueue<Map<String, Object>> providerInputQueue, String jobId,
                                     JobExecution jobExecution) {
        // Read the data from the provider and start working:
        while (provider.hasNext() && jobExecution.isRunning()) {
            Map<String, Object> data = new HashMap<>();
            data.put(DataKey.META.getKey(), createMetaData(jobId, id));
            data.put(DataKey.DATA.getKey(), provider.next());
            boolean added = false;
            while (!added) {
                added = providerInputQueue.offer(data);
                if (!added) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
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
     * Shuts the {@link Provider} and all {@link Action}s of the task down.
     *
     * @param jobId        The job's ID.
     * @param jobExecution Contains information about the job execution.
     */
    private void shutdown(String jobId, JobExecution jobExecution) {
        actions.stream().filter(Action::isActive).forEach(action -> {
            IgorComponentUtil.shutdownServices(action, jobId, id, jobExecution);
            action.shutdown(jobId, id, jobExecution);
        });
        IgorComponentUtil.shutdownServices(provider, jobId, id, jobExecution);
        provider.shutdown(jobId, id, jobExecution);
    }

}
