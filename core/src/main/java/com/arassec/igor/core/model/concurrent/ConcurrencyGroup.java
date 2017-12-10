package com.arassec.igor.core.model.concurrent;

import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.core.model.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controls a concurrency group, i.e. a list of actions that should all be performed by threads in the same thread pool.
 */
public class ConcurrencyGroup {

    private static final String THREAD_NAME_PATTERN = "%s-%d";

    private static final Logger LOG = LoggerFactory.getLogger(ConcurrencyGroup.class);

    private BlockingQueue<IgorData> outputQueue = new LinkedBlockingDeque<>();

    private ExecutorService executorService;

    private String concurrencyGroupId;

    private BlockingQueue<IgorData> inputQueue;

    List<ActionsExecutingRunnable> runnables = new LinkedList<>();

    public ConcurrencyGroup(List<Action> actions, BlockingQueue<IgorData> inputQueue, String concurrencyGroupId) {
        this.inputQueue = inputQueue;
        this.concurrencyGroupId = concurrencyGroupId;

        int numThreads = actions.get(0).getNumThreads();
        if (numThreads == BaseAction.NUM_THREADS_UNDEFINED) {
            numThreads = 1;
        }

        executorService = Executors.newFixedThreadPool(numThreads, new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format(THREAD_NAME_PATTERN, concurrencyGroupId, counter.incrementAndGet()));
            }
        });

        for (int i = 0; i < numThreads; i++) {
            ActionsExecutingRunnable runnable = new ActionsExecutingRunnable(actions, inputQueue, outputQueue);
            runnables.add(runnable);
            executorService.execute(runnable);
        }
    }

    public void shutdown() {
        while (!inputQueue.isEmpty()) {
            LOG.trace("Waiting for threads to finish their work...");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // doesn't matter, just waiting for the threads to finish their jobs.
            }
        }
        runnables.stream().forEach(ActionsExecutingRunnable::shutdown);
        executorService.shutdown();
    }

    public BlockingQueue<IgorData> getOutputQueue() {
        return outputQueue;
    }
}
