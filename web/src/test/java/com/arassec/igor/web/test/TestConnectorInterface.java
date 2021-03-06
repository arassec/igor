package com.arassec.igor.web.test;

import com.arassec.igor.core.model.annotation.IgorSimulationSafe;
import com.arassec.igor.core.model.connector.Connector;

/**
 * A connector interface for testing. The {@link TestConnector} cannot use {@link Connector} directly, since that is filtered
 * during category extraction in the {@link com.arassec.igor.core.application.IgorComponentRegistry}.
 */
public interface TestConnectorInterface {

    /**
     * Returns an integer, even in simulated job runs.
     *
     * @return An integer.
     */
    @IgorSimulationSafe
    Integer simulationSafeMethod();

    /**
     * Returns a String, but only if not invoked during a simulated job run.
     *
     * @return A String if run out of simulation mode.
     */
    String simulationUnsafeMethod();

    /**
     * Method that is annotated as {@link IgorSimulationSafe} in a sub-class.
     *
     * @return A string.
     */
    String directlyAnnotatedSimulationSafeMethod();
}
