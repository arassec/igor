package com.arassec.igor.simulation.job.strategy;

import com.arassec.igor.core.application.simulation.SimulationResult;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;

import java.util.Map;

/**
 * Simulates a job.
 */
public interface SimulationStrategy {

    /**
     * Runs the job in simulation mode.
     *
     * @param job          The job to simulate.
     * @param jobExecution The job's execution information.
     *
     * @return Extracted {@link SimulationResult}s, indexed by the job's, trigger's and action's IDs.
     */
    Map<String, SimulationResult> simulate(Job job, JobExecution jobExecution);

}
