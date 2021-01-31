package com.arassec.igor.simulation.job.strategy;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.trigger.EventTrigger;
import com.arassec.igor.simulation.job.proxy.ProxyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory for {@link SimulationStrategy} instances.
 */
@Component
@RequiredArgsConstructor
public class SimulationStrategyFactory {

    /**
     * A utility for handling proxies around {@link IgorComponent}s.
     */
    private final ProxyProvider proxyProvider;

    /**
     * Determines the strategy to simulate the job.
     *
     * @param job The job to simulate.
     *
     * @return A {@link SimulationStrategy} that can be used to actually simulate a job execution of the supplied job.
     */
    public SimulationStrategy determineSimulationStrategy(Job job) {
        if (job.getTrigger() instanceof EventTrigger) {
            return new EventTriggeredSimulationStrategy(proxyProvider);
        } else {
            return new DefaultSimulationStrategy(proxyProvider);
        }
    }

}
