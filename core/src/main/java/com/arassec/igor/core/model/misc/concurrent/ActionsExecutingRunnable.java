package com.arassec.igor.core.model.misc.concurrent;

import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.core.model.action.Action;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Runnable that uses {@link Action}s to process {@link IgorData} in a separate thread.
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
    private BlockingQueue<IgorData> inputQueue;

    /**
     * The queue where processed data objects are put in.
     */
    private BlockingQueue<IgorData> outputQueue;

    /**
     * Indicates whether this thread should keep working or cancel its work.
     */
    private boolean active = true;

    /**
     * Creates a new ActionsExecutingRunnable.
     *
     * @param actions     The actions of this thread.
     * @param inputQueue  The input queue with incoming data.
     * @param outputQueue The output queue for the processed data.
     */
    public ActionsExecutingRunnable(List<Action> actions, BlockingQueue<IgorData> inputQueue, BlockingQueue<IgorData> outputQueue) {
        this.actions = actions;
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    /**
     * Picks the next data from the input queue, lets all actions process the data, and stores the resulting data in
     * the output queue.
     * <p>
     * This is done until the {@link ActionsExecutingRunnable#shutdown()} method is called.
     */
    @Override
    public void run() {
        try {
            IgorData data;
            while (active) {
                data = inputQueue.poll();
                if (data != null && !data.isEmpty()) {
                    log.debug("Processing: {}", data);
                    boolean continueProcessing = true;
                    for (Action action : actions) {
                        continueProcessing = action.process(data);
                        if (!continueProcessing) {
                            break;
                        }
                    }
                    if (continueProcessing) {
                        outputQueue.put(data);
                    }
                } else {
                    Thread.sleep(500);
                }
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted during action execution!", e);
        }
    }

    /**
     * Shuts this thread down by preventing it to read more data from the input queue.
     */
    public void shutdown() {
        active = false;
    }
}
