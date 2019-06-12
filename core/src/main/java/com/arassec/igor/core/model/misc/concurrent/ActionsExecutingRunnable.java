package com.arassec.igor.core.model.misc.concurrent;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.service.ServiceException;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

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
    public ActionsExecutingRunnable(List<Action> actions, BlockingQueue<Map<String, Object>> inputQueue,
                                    BlockingQueue<Map<String, Object>> outputQueue, JobExecution jobExecution) {
        this.actions = actions;
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.jobExecution = jobExecution;
    }

    /**
     * Picks the next data from the input queue, lets all actions process the data, and stores the resulting data in
     * the output queue.
     * <p>
     * This is done until the {@link ActionsExecutingRunnable#shutdown()} method is called.
     */
    @Override
    public void run() {
        while (active) {
            Map<String, Object> nextInputItem = inputQueue.poll();
            if (nextInputItem != null && !nextInputItem.isEmpty()) {

                List<Map<String, Object>> items = new LinkedList<>();
                items.add(nextInputItem);

                process(actions, items);
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // Doesn't matter, we just waited for the next piece of work...
                }
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
     * Processes the data with the supplied actions.
     *
     * @param actions The actions to apply to the data.
     * @param items   The data to process.
     */
    private void process(List<Action> actions, List<Map<String, Object>> items) {
        if (actions == null || items == null) {
            return;
        }

        for (Action action : actions) {

            if (!active) {
                return;
            }

            log.debug("Running Action: {}", action.getClass().getName());
            List<Map<String, Object>> actionResult = new LinkedList<>();

            for (Map<String, Object> item : items) {
                if (!active) {
                    return;
                }

                log.trace("Processing: {}", item);
                List<Map<String, Object>> partialActionResult = action.process(item, false, jobExecution);
                if (partialActionResult != null && !partialActionResult.isEmpty()) {
                    actionResult.addAll(partialActionResult);
                }
            }

            items.clear();

            if (actionResult == null || actionResult.isEmpty()) {
                break;
            } else {
                items.addAll(actionResult);
            }
        }

        putToOutputQueue(items);
    }

    /**
     * Puts all supplied items in the output queue.
     *
     * @param items The items to output.
     */
    private void putToOutputQueue(List<Map<String, Object>> items) {
        if (!items.isEmpty()) {
            for (Map<String, Object> outputItem : items) {
                if (!active) {
                    return;
                }
                try {
                    outputQueue.put(outputItem);
                } catch (InterruptedException e) {
                    throw new ServiceException("Interrupted while putting data to the output queue!", e);
                }
            }
        }
    }

    /**
     * Shuts this thread down by preventing it to read more data from the input queue.
     */
    public void shutdown() {
        active = false;
    }

}
