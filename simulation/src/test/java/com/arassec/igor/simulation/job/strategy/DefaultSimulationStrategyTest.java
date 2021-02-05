package com.arassec.igor.simulation.job.strategy;

import com.arassec.igor.core.application.simulation.SimulationResult;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.simulation.job.proxy.ProxyProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link DefaultSimulationStrategy}.
 */
@DisplayName("Default Simulation-Strategy Tests.")
class DefaultSimulationStrategyTest {

    /**
     * Tests simulating a job with the strategy.
     */
    @Test
    @DisplayName("Tests simulating a job with the strategy.")
    void testSimulate() {
        ProxyProvider proxyProviderMock = mock(ProxyProvider.class);
        Job jobMock = mock(Job.class);
        JobExecution jobExecution = new JobExecution();

        DefaultSimulationStrategy strategy = new DefaultSimulationStrategy(proxyProviderMock);

        Map<String, SimulationResult> result = strategy.simulate(jobMock, jobExecution);

        assertNotNull(result);
        verify(proxyProviderMock, times(1)).applyProxies(jobMock);
        verify(jobMock, times(1)).start(jobExecution);
    }

}
