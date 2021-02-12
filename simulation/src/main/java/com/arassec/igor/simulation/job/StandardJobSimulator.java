package com.arassec.igor.simulation.job;

import com.arassec.igor.application.simulation.JobSimulator;
import com.arassec.igor.application.simulation.SimulationResult;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.trigger.EventTrigger;
import com.arassec.igor.core.util.event.JobTriggerEvent;
import com.arassec.igor.simulation.IgorSimulationProperties;
import com.arassec.igor.simulation.job.strategy.SimulationStrategy;
import com.arassec.igor.simulation.job.strategy.SimulationStrategyFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A {@link JobSimulator} that runs the jobs in simulation mode.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StandardJobSimulator implements JobSimulator {

    /**
     * Contains the currently simulated jobs, indexed by their ID.
     */
    @Getter
    private final List<Job> runningSimulations = Collections.synchronizedList(new LinkedList<>());

    /**
     * Simulation configuration properties.
     */
    private final IgorSimulationProperties simulationProperties;

    /**
     * A factory for {@link SimulationStrategy} instances.
     */
    private final SimulationStrategyFactory simulationStrategyFactory;

    /**
     * {@inheritDoc}
     */
    @Async
    @Override
    public Future<Map<String, SimulationResult>> simulateJob(Job job) {

        JobExecution jobExecution = new JobExecution();

        runningSimulations.add(job);

        SimulationStrategy simulationStrategy = simulationStrategyFactory.determineSimulationStrategy(job);

        Map<String, SimulationResult> simulationResults = simulationStrategy.simulate(job, jobExecution);

        runningSimulations.remove(job);

        return new AsyncResult<>(simulationResults);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelAllSimulations(String jobId) {
        List<Job> jobsToCancel = runningSimulations.stream()
                .filter(simulatedJob -> simulatedJob.getId().equals(jobId))
                .collect(Collectors.toList());
        jobsToCancel.forEach(Job::cancel);
    }

    /**
     * Listens for job trigger events and starts processing in the simulated job.
     *
     * @param jobTriggerEvent The event containing additional data.
     */
    @EventListener
    public void onJobTriggerEvent(JobTriggerEvent jobTriggerEvent) {
        runningSimulations.stream()
                .filter(job -> job.getId().equals(jobTriggerEvent.getJobId()))
                .filter(job -> job.getTrigger() instanceof EventTrigger)
                .filter(job -> ((EventTrigger) job.getTrigger()).getSupportedEventType().equals(jobTriggerEvent.getEventType()))
                .forEach(job -> ((EventTrigger) job.getTrigger()).processEvent(jobTriggerEvent.getEventData()));
    }

    /**
     * Cancels simulations that haven't received any input data for a configurable amount of time.
     */
    @Scheduled(fixedRate = 1000)
    public void cancelStaleSimulations() {
        Predicate<Job> isStale = job -> {
            if (job.getCurrentJobExecution() == null) {
                return true;
            }
            Instant started = job.getCurrentJobExecution().getStarted();
            Instant now = Instant.now();
            Duration time = Duration.between(started, now);
            return time.toSeconds() >= simulationProperties.getTimeout();
        };
        List<Job> staleSimulations = runningSimulations.stream()
                .filter(isStale)
                .collect(Collectors.toList());
        staleSimulations.forEach(job -> {
            log.debug("Cancelling simulation of job '" + job.getId() + "' due to timeout!");
            job.cancel();
        });
    }

}
