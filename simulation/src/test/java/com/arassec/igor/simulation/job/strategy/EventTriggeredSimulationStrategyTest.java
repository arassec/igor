package com.arassec.igor.simulation.job.strategy;


import com.arassec.igor.core.application.simulation.SimulationResult;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.simulation.job.proxy.ProxyProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link EventTriggeredSimulationStrategy}.
 */
@DisplayName("Event-Triggered Simulation-Strategy Tests.")
class EventTriggeredSimulationStrategyTest {

    /**
     * Tests simulating a job with the strategy.
     */
    @Test
    @DisplayName("Tests simulating a job with the strategy.")
    void testSimulate() {
        ProxyProvider proxyProviderMock = mock(ProxyProvider.class);
        Job jobMock = mock(Job.class);
        JobExecution jobExecution = new JobExecution();

        doAnswer(invocationOnMock -> {
            jobExecution.setProcessedEvents(1);
            return null;
        }).when(jobMock).start(eq(jobExecution));

        EventTriggeredSimulationStrategy strategy = new EventTriggeredSimulationStrategy(proxyProviderMock);

        Map<String, SimulationResult> result = strategy.simulate(jobMock, jobExecution);

        assertNotNull(result);
        verify(proxyProviderMock, times(1)).applyProxies(eq(jobMock));
    }

}
