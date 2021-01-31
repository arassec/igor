package com.arassec.igor.core.application.simulation;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.util.event.JobTriggerEvent;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * Manages simulated executions of jobs.
 */
public interface JobSimulator {

    /**
     * Starts the job in simulation mode in a separate thread.
     *
     * @param job The job to simulate.
     *
     * @return A {@link Future} containing the simulation results after the execution finishes.
     */
    Future<Map<String, SimulationResult>> simulateJob(Job job);

    /**
     * Cancels all running simulations of the provided job.
     *
     * @param jobId The job's ID.
     */
    void cancelAllSimulations(String jobId);

    /**
     * Cancels all running simulations that are active for too long without processing any events.
     */
    void cancelStaleSimulations();

    /**
     * Called by Spring if a {@link JobTriggerEvent} is fired. Takes care, that the event is handed over to the correct job, if it
     * is currently executed in simulation mode.
     *
     * @param jobTriggerEvent The event to hand over to a simulated job.
     */
    @SuppressWarnings("unused")
    void onJobTriggerEvent(JobTriggerEvent jobTriggerEvent);

}
