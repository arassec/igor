package com.arassec.igor.core.model.provider;

import com.arassec.igor.core.model.IgorComponent;

import java.util.Map;

/**
 * Provides data for a job to process.
 */
public interface Provider extends IgorComponent {

    /**
     * Returns {@code true}, if there is further data to process.
     *
     * @return {@code true}, if there is data to process, {@code false} otherwise.
     */
    boolean hasNext();

    /**
     * Returns the next data-piece to process.
     *
     * @return A JSON-Object that contains the data to process.
     */
    Map<String, Object> next();

    /**
     * The limit for test data during simulated job runs of this provider.
     *
     * @return The maximum number of data sets to use during simulations.
     */
    int getSimulationLimit();

}
