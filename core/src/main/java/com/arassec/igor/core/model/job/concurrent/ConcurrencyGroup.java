package com.arassec.igor.core.model.job.concurrent;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.execution.JobExecution;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controls a concurrency group, i.e. a list of {@link Action}s that should all be performed with the same number of threads.
 */
@Slf4j
public class ConcurrencyGroup implements Thread.UncaughtExceptionHandler {

    /**
     * Defines the name of the threads in the pool of this concurrency-group.
     */
    private static final String THREAD_NAME_PATTERN = "%s-%d";

    /**
     * Contains the incoming data for this concurrency-group. This is the output-queue of the previous concurrency-group.
     */
    private final BlockingQueue<Map<String, Object>> inputQueue;

    /**
     * Contains the output of this concurrency-group, which models the input for the following concurrency-group.
     */
    private final BlockingQueue<Map<String, Object>> outputQueue = new LinkedBlockingDeque<>();

    /**
     * The {@link ThreadPoolExecutor} managing the threads.
     */
    private final ThreadPoolExecutor threadPoolExecutor;

    /**
     * The ID of this concurrency-group. Only used for logging purposes to identify this concureency-group.
     */
    private final String concurrencyGroupId;

    /**
     * The runnables that invoke the actions.
     */
    private final List<ActionsExecutingRunnable> runnables = new LinkedList<>();

    /**
     * The {@link JobExecution} contains information about the state of the current job run. Required here because an exception in
     * a thread should stop the whole job, which runs in another thread.
     */
    @Getter
    @Setter
    private JobExecution jobExecution;

    /**
     * Creates a new concurrency-group.
     *
     * @param actions            The list of {@link Action}s that are contained in this group.
     * @param inputQueue         The input for this concurrency-group. Data is read from this queue and handed over to the
     *                           actions. The output of the last action is put into the output queue.
     * @param concurrencyGroupId The ID of this concurrency-group.
     * @param jobExecution       The {@link JobExecution} containing the current state of the job run.
     * @param numThreads         The number of threads the job' actions should execute with.
     */
    public ConcurrencyGroup(List<Action> actions, BlockingQueue<Map<String, Object>> inputQueue, String concurrencyGroupId,
                            JobExecution jobExecution, int numThreads) {
        this.inputQueue = inputQueue;
        this.concurrencyGroupId = concurrencyGroupId;
        this.jobExecution = jobExecution;

        int threads = numThreads;
        if (actions != null && !actions.isEmpty() && actions.get(0).enforceSingleThread()) {
            threads = 1;
        }

        threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads, new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger();

            @Override
            @SuppressWarnings("NullableProblems")
            public Thread newThread(Runnable r) {
                var t = new Thread(r, String.format(THREAD_NAME_PATTERN, concurrencyGroupId, counter.incrementAndGet()));
                t.setUncaughtExceptionHandler(ConcurrencyGroup.this);
                return t;
            }
        });

        for (var i = 0; i < threads; i++) {
            var runnable = new ActionsExecutingRunnable(actions, inputQueue, outputQueue, jobExecution);
            runnables.add(runnable);
            threadPoolExecutor.execute(runnable);
        }
    }

    /**
     * Calls {@link Action#complete()} on all actions of this concurrency group.
     */
    public void complete() {
        waitForEmptyInputQueue();
        runnables.forEach(ActionsExecutingRunnable::complete);
    }

    /**
     * Shuts the thread pool down after all incoming data has been processed. Threads might still run after calling this method if
     * e.g. a large file is copied in an action.
     * <p>
     * In case the job is cancelled, this method will not wait for all data to be processed, but shut the thread pool down
     * immediately.
     */
    public void shutdown() {
        waitForEmptyInputQueue();
        runnables.forEach(ActionsExecutingRunnable::shutdown);
        threadPoolExecutor.shutdown();
    }

    /**
     * Awaits the termination of the last threads in the thread pool.
     * <p>
     * If the job is cancelled, or in case of interruptions, all running threads will be stopped immediately.
     *
     * @return {@code true}, if all threads in the group have been terminated. {@code false} otherwise.
     */
    public boolean awaitTermination() {
        log.debug("Total/Active/Completed Threads in '{}': {}/{}/{}", concurrencyGroupId, threadPoolExecutor.getPoolSize(),
            threadPoolExecutor.getActiveCount(), threadPoolExecutor.getCompletedTaskCount());
        try {
            if (jobExecution != null && !jobExecution.isRunningOrActive()) {
                threadPoolExecutor.shutdownNow();
                return true;
            }
            boolean awaitTerminationResult = threadPoolExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS);
            log.debug("After awaitTermination: Total/Active/Completed Threads: {}/{}/{}", threadPoolExecutor.getPoolSize(),
                threadPoolExecutor.getActiveCount(), threadPoolExecutor.getCompletedTaskCount());
            return awaitTerminationResult;
        } catch (InterruptedException e) {
            log.error("Concurrency-Group interrupted during awaitTermination()!", e);
            threadPoolExecutor.shutdownNow();
            Thread.currentThread().interrupt();
            return true;
        }
    }

    /**
     * Returns the output queue for the following concurrency-group.
     *
     * @return The {@link BlockingQueue} with the output of this group's actions.
     */
    public BlockingQueue<Map<String, Object>> getOutputQueue() {
        return outputQueue;
    }

    /**
     * Catches exceptions thrown by threads of this concurrency-group.
     * <p>
     * If the job is already cancelled, this method does nothing.
     *
     * @param thread    The thread that threw the exception.
     * @param throwable The throwbale thrown by the thread.
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (jobExecution != null && jobExecution.isRunningOrActive()) {
            jobExecution.fail(throwable);
            log.error("Exception caught in ConcurrencyGroup!", throwable);
        }
    }

    /**
     * Waits until the input queue has been emptied by the worker threads.
     */
    private void waitForEmptyInputQueue() {
        if (jobExecution != null && !jobExecution.isRunningOrActive()) {
            log.debug("Job cancelled. Not waiting for empty input queue in concurrency group: {}", concurrencyGroupId);
            return;
        }

        var waitLock = new Object();
        ScheduledExecutorService waitCheckExecutor = Executors.newSingleThreadScheduledExecutor();

        waitCheckExecutor.scheduleAtFixedRate(() -> {
            synchronized (waitLock) {
                log.trace("Checking for threads to finish their work to complete concurrency-group: {} ({})",
                    concurrencyGroupId, inputQueue.size());
                if (inputQueue.isEmpty()) {
                    waitLock.notifyAll();
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        synchronized (waitLock) {
            try {
                while (!inputQueue.isEmpty()) {
                    waitLock.wait();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
