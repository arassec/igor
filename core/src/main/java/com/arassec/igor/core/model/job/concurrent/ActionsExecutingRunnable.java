package com.arassec.igor.core.model.job.concurrent;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.execution.JobExecution;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Runnable that uses {@link Action}s to process data in a separate thread.
 */
@Slf4j
public class ActionsExecutingRunnable implements Runnable {

    /**
     * The actions of this thread.
     */
    private List<Action> actions;

    /**
     * The queue with incoming data objects.
     */
    private BlockingQueue<Map<String, Object>> inputQueue;

    /**
     * The queue where processed data objects are put in.
     */
    private BlockingQueue<Map<String, Object>> outputQueue;

    /**
     * Indicates whether this thread should keep working or cancel its work.
     */
    private boolean active = true;

    /**
     * The job execution.
     */
    private JobExecution jobExecution;

    /**
     * Creates a new ActionsExecutingRunnable.
     *
     * @param actions     The actions of this thread.
     * @param inputQueue  The input queue with incoming data.
     * @param outputQueue The output queue for the processed data.
     */
    ActionsExecutingRunnable(List<Action> actions, BlockingQueue<Map<String, Object>> inputQueue,
                             BlockingQueue<Map<String, Object>> outputQueue, JobExecution jobExecution) {
        if (inputQueue == null) {
            throw new IllegalArgumentException("InputQueue required!");
        }

        if (outputQueue == null) {
            throw new IllegalArgumentException("OutputQueue required!");
        }

        this.actions = actions;
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.jobExecution = jobExecution;
    }

    /**
     * Picks the next data from the input queue, lets all actions process the data, and stores the resulting data in the output
     * queue.
     * <p>
     * This is done until the {@link ActionsExecutingRunnable#shutdown()} method is called.
     */
    @Override
    public void run() {
        while (active) {
            try {
                Map<String, Object> nextInputItem = inputQueue.poll(500, TimeUnit.MILLISECONDS);
                if (nextInputItem != null && !nextInputItem.isEmpty()) {
                    process(actions, List.of(nextInputItem));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Completes all actions and processes final data, if any.
     */
    public void complete() {
        for (int i = 0; i < actions.size(); i++) {
            Action action = actions.get(i);
            List<Map<String, Object>> items = action.complete();
            if (items != null && !items.isEmpty()) {
                if ((i + 1) < actions.size()) {
                    process(actions.subList(i + 1, actions.size()), items);
                } else {
                    // The last action. Output goes directly to the output queue:
                    putToOutputQueue(items);
                }
            }
        }
    }

    /**
     * Resets all actions.
     */
    public void reset() {
        actions.forEach(Action::reset);
    }

    /**
     * Shuts this thread down by preventing it to read more data from the input queue.
     */
    public void shutdown() {
        active = false;
    }

    /**
     * Processes the data with the supplied actions.
     *
     * @param actions The actions to apply to the data.
     * @param items   The data to process.
     */
    private void process(List<Action> actions, List<Map<String, Object>> items) {
        if (actions == null || items == null) {
            return;
        }

        List<Map<String, Object>> workingItems = new LinkedList<>(items);

        for (Action action : actions) {

            List<Map<String, Object>> actionResult = new LinkedList<>();

            for (Map<String, Object> workingItem : workingItems) {
                log.trace("Processing: {}", workingItem);
                List<Map<String, Object>> partialActionResult = action.process(workingItem, jobExecution);
                if (partialActionResult != null && !partialActionResult.isEmpty()) {
                    actionResult.addAll(partialActionResult);
                }
            }

            workingItems.clear();

            if (actionResult.isEmpty()) {
                break;
            } else {
                workingItems.addAll(actionResult);
            }
        }

        putToOutputQueue(workingItems);
    }

    /**
     * Puts all supplied items in the output queue.
     *
     * @param items The items to output.
     */
    private void putToOutputQueue(List<Map<String, Object>> items) {
        if (!items.isEmpty()) {
            for (Map<String, Object> outputItem : items) {
                try {
                    outputQueue.put(outputItem);
                } catch (InterruptedException e) {
                    log.error("Interrupted while putting data to the output queue!", e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}
