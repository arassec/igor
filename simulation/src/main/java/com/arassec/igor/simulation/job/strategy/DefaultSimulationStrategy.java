package com.arassec.igor.simulation.job.strategy;

import com.arassec.igor.core.application.simulation.SimulationResult;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.simulation.job.proxy.ProxyProvider;

import java.util.Map;

/**
 * Default {@link SimulationStrategy}.
 */
public class DefaultSimulationStrategy extends BaseSimulationStrategy {

    /**
     * Creates a new instance.
     *
     * @param proxyProvider The util to proxy igor components.
     */
    public DefaultSimulationStrategy(ProxyProvider proxyProvider) {
        super(proxyProvider);
    }

    /**
     * Simply starts the job.
     *
     * @param job          The job to simulate.
     * @param jobExecution The job's execution information.
     */
    @Override
    public Map<String, SimulationResult> simulate(Job job, JobExecution jobExecution) {
        proxyProvider.applyProxies(job);
        job.start(jobExecution);
        return extractSimulationResult(job, jobExecution);
    }

}
