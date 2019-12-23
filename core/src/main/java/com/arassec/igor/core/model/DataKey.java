package com.arassec.igor.core.model;

import lombok.Getter;

/**
 * Contains common data keys.
 */
public enum DataKey {

    /**
     * The key for the meta-data.
     */
    META("meta"),

    /**
     * The key for the provider's data.
     */
    DATA("data"),

    /**
     * The key to the job's ID in the meta-data.
     */
    JOB_ID("jobId"),

    /**
     * The key to the task's ID in the meta-data.
     */
    TASK_ID("taskId"),

    /**
     * The key to the current timestamp in the meta-data.
     */
    TIMESTAMP("timestamp"),

    /**
     * The key for the simulation property.
     */
    SIMULATION("simulation"),

    /**
     * The key for simulation log entries.
     */
    SIMULATION_LOG("simulationLog");

    /**
     * The key as String.
     */
    @Getter
    private String key;

    /**
     * Creates a new instance.
     *
     * @param key The key as String.
     */
    DataKey(String key) {
        this.key = key;
    }

}
