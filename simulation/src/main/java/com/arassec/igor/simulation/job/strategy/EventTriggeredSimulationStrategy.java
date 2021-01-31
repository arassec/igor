package com.arassec.igor.simulation.job.strategy;

import com.arassec.igor.core.application.simulation.SimulationResult;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.simulation.job.proxy.ProxyProvider;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * {@link SimulationStrategy} for jobs that are triggered by events.
 */
public class EventTriggeredSimulationStrategy extends BaseSimulationStrategy {

    /**
     * Creates a new instance.
     *
     * @param proxyProvider The util to proxy igor components.
     */
    public EventTriggeredSimulationStrategy(ProxyProvider proxyProvider) {
        super(proxyProvider);
    }

    /**
     * Starts the job in a separate thread and awaits the retrieval of at least on event for that job.
     *
     * @param job          The job to simulate.
     * @param jobExecution The job's execution information.
     */
    @Override
    public Map<String, SimulationResult> simulate(Job job, JobExecution jobExecution) {
        proxyProvider.applyProxies(job);

        ExecutorService jobExecutor = Executors.newSingleThreadExecutor();
        jobExecutor.submit(() -> job.start(jobExecution));

        Object processedEventsLock = new Object();

        ScheduledExecutorService jobFinishedCheckExecutor = Executors.newSingleThreadScheduledExecutor();
        jobFinishedCheckExecutor.scheduleAtFixedRate(() -> {
            synchronized (processedEventsLock) {
                if (jobExecution.getProcessedEvents() > 0 || (jobExecution.getStarted() != null && !jobExecution.isRunningOrActive())) {
                    processedEventsLock.notifyAll();
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        synchronized (processedEventsLock) {
            try {
                while (jobExecution.getProcessedEvents() == 0 && (jobExecution.getStarted() == null ||
                        jobExecution.isRunningOrActive())) {
                    processedEventsLock.wait();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        job.cancel();

        jobExecutor.shutdown();
        jobFinishedCheckExecutor.shutdown();

        return extractSimulationResult(job, jobExecution);
    }

}
