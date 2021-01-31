package com.arassec.igor.simulation.job.strategy;

import com.arassec.igor.core.application.simulation.SimulationResult;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.simulation.job.proxy.ActionProxy;
import com.arassec.igor.simulation.job.proxy.TriggerProxy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link BaseSimulationStrategy}.
 */
@DisplayName("Tests the 'Base Simulation-Strategy'.")
class BaseSimulationStrategyTest {

    /**
     * The class under test.
     */
    private static final BaseSimulationStrategy baseSimulationStrategy = mock(BaseSimulationStrategy.class, CALLS_REAL_METHODS);

    /**
     * Tests extracting simulation results from igor component proxies.
     */
    @Test
    @DisplayName("Tests extracting simulation results from igor component proxies.")
    void testExtractSimulationResult() {
        List<Map<String, Object>> triggerData = List.of(Map.of("trigger", "data"));
        TriggerProxy triggerProxy = mock(TriggerProxy.class);
        when(triggerProxy.getSimulationTriggerData()).thenReturn(triggerData);

        List<Map<String, Object>> actionData = List.of(Map.of("action", "data"));
        ActionProxy actionProxy = mock(ActionProxy.class);
        when(actionProxy.getId()).thenReturn("action-id");
        when(actionProxy.getCollectedData()).thenReturn(actionData);
        when(actionProxy.getErrorCause()).thenReturn("test-action-error");

        Job job = Job.builder().id("job-id").trigger(triggerProxy).actions(List.of(actionProxy)).build();

        JobExecution jobExecution = new JobExecution();
        jobExecution.setErrorCause("test-job-error");

        Map<String, SimulationResult> result = baseSimulationStrategy.extractSimulationResult(job, jobExecution);

        SimulationResult jobResult = result.get("job-id");
        assertEquals("test-job-error", jobResult.getErrorCause());
        assertEquals(triggerData.get(0).toString(), jobResult.getResults().get(0).get("data").toString());

        SimulationResult actionResult = result.get("action-id");
        assertEquals("test-action-error", actionResult.getErrorCause());
        assertEquals(actionData.get(0).toString(), actionResult.getResults().get(0).toString());
    }

    /**
     * Tests extracting simulation results from a 'bare' igor job.
     */
    @Test
    @DisplayName("Tests extracting simulation results from a 'bare' igor job.")
    void testExtractSimulationResultNoProxies() {
        Job job = Job.builder().trigger(mock(Trigger.class)).actions(List.of(mock(Action.class))).build();
        Map<String, SimulationResult> result = baseSimulationStrategy.extractSimulationResult(job, new JobExecution());
        assertTrue(result.isEmpty());
    }

}
