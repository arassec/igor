package com.arassec.igor.core.model.concurrent;

import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.service.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controls a concurrency group, i.e. a list of actions that should all be performed by threads in the same thread pool.
 */
@Slf4j
public class ConcurrencyGroup implements Thread.UncaughtExceptionHandler {

    private static final String THREAD_NAME_PATTERN = "%s-%d";

    private BlockingQueue<IgorData> outputQueue = new LinkedBlockingDeque<>();

    private ExecutorService executorService;

    private String concurrencyGroupId;

    private BlockingQueue<IgorData> inputQueue;

    List<ActionsExecutingRunnable> runnables = new LinkedList<>();

    private JobExecution jobExecution;

    public ConcurrencyGroup(List<Action> actions, BlockingQueue<IgorData> inputQueue, String concurrencyGroupId, JobExecution jobExecution) {
        this.inputQueue = inputQueue;
        this.concurrencyGroupId = concurrencyGroupId;

        int numThreads = actions.get(0).getNumThreads();
        if (numThreads == BaseAction.NUM_THREADS_UNDEFINED) {
            numThreads = 1;
        }

        this.jobExecution = jobExecution;

        executorService = Executors.newFixedThreadPool(numThreads, new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, String.format(THREAD_NAME_PATTERN, concurrencyGroupId, counter.incrementAndGet()));
                t.setUncaughtExceptionHandler(ConcurrencyGroup.this);
                return t;
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
            if (jobExecution.cancelled()) {
                log.debug("Job cancelled. Shutting down concurrency-group '{}' immediately!", concurrencyGroupId);
                break;
            }
            log.trace("Waiting for threads to finish their work...");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // doesn't matter, just waiting for the threads to finish their jobs.
            }
        }
        runnables.stream().forEach(ActionsExecutingRunnable::shutdown);
        executorService.shutdown();
    }

    public boolean awaitTermination() {
        try {
            if (jobExecution.cancelled()) {
                executorService.shutdownNow();
                return true;
            }
            return executorService.awaitTermination(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Concurrency-Group interrupted during awaitTermination()!", e);
            executorService.shutdownNow();
            return true;
        }
    }

    public BlockingQueue<IgorData> getOutputQueue() {
        return outputQueue;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (!jobExecution.cancelled()) {
            jobExecution.fail(e);
            log.error("Exception caught in ConcurrencyGroup!", e);
        }
    }
}
