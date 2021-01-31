package com.arassec.igor.simulation.job.strategy;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.trigger.EventTrigger;
import com.arassec.igor.simulation.job.proxy.ProxyProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link SimulationStrategyFactory}.
 */
@DisplayName("Tests the Simulation-Strategy factory.")
class SimulationStrategyFactoryTest {

    /**
     * Tests creating a simulation strategy.
     */
    @Test
    @DisplayName("Tests creating a simulation strategy.")
    void testDetermineSimulationStrategy() {
        SimulationStrategyFactory factory = new SimulationStrategyFactory(new ProxyProvider());

        SimulationStrategy strategy = factory.determineSimulationStrategy(Job.builder().build());
        assertTrue(strategy instanceof DefaultSimulationStrategy);

        strategy = factory.determineSimulationStrategy(Job.builder().trigger(mock(EventTrigger.class)).build());
        assertTrue(strategy instanceof EventTriggeredSimulationStrategy);
    }

}
