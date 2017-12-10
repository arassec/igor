package com.arassec.igor.core.model.provider;

/**
 * Provides data for a job to process.
 * <p>
 * Created by Andreas Sensen on 21.05.2017.
 */
public interface Provider {

    /**
     * Initializes the provider.
     */
    void initialize();

    /**
     * Returns {@code true}, if there is further data to process.
     *
     * @return {@code true}, if there is data to process, {@code false} otherwise.
     */
    boolean hasNext();

    /**
     * Returns the next data-piece to process.
     *
     * @return An {@code IgorData}-Object that contains the data to process.
     */
    IgorData next();

}
