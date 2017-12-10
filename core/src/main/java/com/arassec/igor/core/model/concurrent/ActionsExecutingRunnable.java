package com.arassec.igor.core.model.concurrent;

import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.core.model.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * TODO: Document class.
 */
public class ActionsExecutingRunnable implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ActionsExecutingRunnable.class);

    private List<Action> actions;

    private BlockingQueue<IgorData> inputQueue;

    private BlockingQueue<IgorData> outputQueue;

    private boolean active = true;

    public ActionsExecutingRunnable(List<Action> actions, BlockingQueue<IgorData> inputQueue, BlockingQueue<IgorData> outputQueue) {
        this.actions = actions;
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {
        try {
            IgorData data;
            while(active) {
                data = inputQueue.poll();
                if (data != null && !data.isEmpty()) {
                    LOG.debug("Processing: {}", data);
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

    public void shutdown() {
        active = false;
    }
}
