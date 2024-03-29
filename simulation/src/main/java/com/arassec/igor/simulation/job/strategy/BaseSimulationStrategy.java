package com.arassec.igor.simulation.job.strategy;

import com.arassec.igor.application.simulation.SimulationResult;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.simulation.job.proxy.ActionProxy;
import com.arassec.igor.simulation.job.proxy.ProxyProvider;
import com.arassec.igor.simulation.job.proxy.TriggerProxy;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Base class for {@link SimulationStrategy}.
 */
@RequiredArgsConstructor
public abstract class BaseSimulationStrategy implements SimulationStrategy {

    /**
     * A provider creating proxies around {@link IgorComponent}s.
     */
    protected final ProxyProvider proxyProvider;

    /**
     * Extracts the simulation results from the supplied job. The job must have been enhanced with proxies around igor components
     * (by using {@link ProxyProvider#applyProxies(Job)}), and simulated using a {@link SimulationStrategy}, prior to using this
     * method!
     *
     * @param job          The job to extract the simulation data from.
     * @param jobExecution The simulated job's execution information.
     * @return Extracted {@link SimulationResult}s, indexed by the job's, trigger's and action's IDs.
     */
    protected Map<String, SimulationResult> extractSimulationResult(Job job, JobExecution jobExecution) {
        Map<String, SimulationResult> result = new HashMap<>();

        var jobResult = new SimulationResult();

        if (jobExecution.getErrorCause() != null) {
            jobResult.setErrorCause(jobExecution.getErrorCause());
        }

        if (job.getTrigger() instanceof TriggerProxy triggerProxy) {
            final List<Map<String, Object>> simulationTriggerData = triggerProxy.getCollectedData();
            simulationTriggerData.forEach(dataItem -> jobResult.getResults().add(dataItem));
        }

        job.getActions().forEach(action -> {
            if (action instanceof ActionProxy actionProxy) {
                var actionResult = new SimulationResult();
                actionResult.setErrorCause(actionProxy.getErrorCause());
                actionProxy.getCollectedData().stream().filter(Objects::nonNull).forEach(jsonObject -> actionResult.getResults().add(jsonObject));
                if (action.getId() != null) {
                    result.put(action.getId(), actionResult);
                }
            }
        });

        if (StringUtils.hasText(jobResult.getErrorCause()) || !jobResult.getResults().isEmpty()) {
            result.put(job.getId(), jobResult);
        }

        return result;
    }

}
