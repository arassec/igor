package com.arassec.igor.web.test;

import com.arassec.igor.core.model.annotation.IgorSimulationSafe;
import com.arassec.igor.core.model.connector.Connector;

/**
 * A connector interface for testing. The {@link TestConnector} cannot use {@link Connector} directly, since that is filtered
 * during category extraction in the {@link com.arassec.igor.application.registry.IgorComponentRegistry}.
 */
public interface TestConnectorInterface {

    /**
     * Returns an integer, even in simulated job runs.
     *
     * @return An integer.
     */
    @IgorSimulationSafe
    @SuppressWarnings("unused")
    Integer simulationSafeMethod();

    /**
     * Returns a String, but only if not invoked during a simulated job run.
     *
     * @return A String if run out of simulation mode.
     */
    @SuppressWarnings("unused")
    String simulationUnsafeMethod();

    /**
     * Method that is annotated as {@link IgorSimulationSafe} in a subclass.
     *
     * @return A string.
     */
    @SuppressWarnings("unused")
    String directlyAnnotatedSimulationSafeMethod();

}
