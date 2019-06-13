package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.dryrun.DryRunActionResult;
import com.arassec.igor.core.model.job.dryrun.DryRunJobResult;
import com.arassec.igor.core.model.job.dryrun.DryRunTaskResult;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.misc.concurrent.ConcurrencyGroup;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.util.StacktraceFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
     * Limits the results in a dry-run.
     */
    private int dryrunLimit = 25;

    /**
     * Creates a new Task.
     *
     * @param id The task's ID.
     */
    public Task(String id) {
        this.id = id;
    }

    /**
     * Runs the task by first creating concurrency-groups for actions that should run with the same amount of threads
     * and then requesting the provider for data to process.
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
            Map<String, Object> data = provider.next();
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
     * Performs a dry-run of the task collecting data.
     *
     * @param result The target object to store results in.
     * @param jobId  The ID of the job currently executing.
     */
    void dryRun(DryRunJobResult result, Long jobId) {

        DryRunTaskResult taskResult = new DryRunTaskResult();

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            JobExecution jobExecution = new JobExecution();

            provider.initialize(jobId, id, jobExecution);

            actions.forEach(Action::initialize);

            List<Map<String, Object>> providerResult = new LinkedList<>();

            int counter = 0;
            while (provider.hasNext() && counter < dryrunLimit) {
                Map<String, Object> item = provider.next();
                providerResult.add(item);
                taskResult.getResults().add(objectMapper.readValue(new JSONObject(item).toString(), HashMap.class));
                counter++;
            }

            for (Action action : actions) {
                DryRunActionResult actionResult = new DryRunActionResult();

                try {
                    if (!action.isActive()) {
                        Map<String, Object> item = new HashMap<>();
                        item.put(Action.JOB_ID_KEY, jobId);
                        item.put(Action.TASK_ID_KEY, getId());
                        item.put(Action.DRY_RUN_COMMENT_KEY, "Action is disabled.");
                        actionResult.getResults().add(item);
                    } else {
                        if (providerResult.isEmpty()) {
                            Map<String, Object> item = new HashMap<>();
                            item.put(Action.JOB_ID_KEY, jobId);
                            item.put(Action.TASK_ID_KEY, getId());
                            item.put(Action.DRY_RUN_COMMENT_KEY, "No data to process.");
                            actionResult.getResults().add(item);
                        } else {
                            List<Map<String, Object>> actionItems = new LinkedList<>();
                            for (Map<String, Object> item : providerResult) {
                                List<Map<String, Object>> items = action.process(item, true, jobExecution);
                                if (items != null && !items.isEmpty()) {
                                    actionItems.addAll(items);
                                }
                            }

                            List<Map<String, Object>> items = action.complete();
                            if (items != null && !items.isEmpty()) {
                                actionItems.addAll(items);
                            }

                            providerResult.clear();

                            if (!actionItems.isEmpty()) {
                                for (Map<String, Object> item : actionItems) {
                                    actionResult.getResults()
                                            .add(objectMapper.readValue(new JSONObject(item).toString(), HashMap.class));
                                }
                                providerResult.addAll(actionItems);
                            }
                        }
                    }
                } catch (Exception e) {
                    actionResult.setErrorCause(StacktraceFormatter.format(e));
                }

                taskResult.getActionResults().add(actionResult);
            }

            actions.forEach(action -> action.shutdown(jobId, id));

        } catch (Exception e) {
            taskResult.setErrorCause(StacktraceFormatter.format(e));
        }

        result.getTaskResults().add(taskResult);
    }

}
