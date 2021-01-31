package com.arassec.igor.simulation.job.proxy.test;


import com.arassec.igor.core.model.annotation.IgorSimulationSafe;
import com.arassec.igor.core.model.connector.BaseConnector;

/**
 * A connector for testing.
 */
public class TestConnector extends BaseConnector {

    /**
     * Should return {@code null} in simulation mode.
     */
    public String notSimulationSafe() {
        return "fail";
    }

    /**
     * Should return true in simulation mode.
     *
     * @return Always {@code true}.
     */
    @IgorSimulationSafe
    public boolean simulationSafe() {
        return true;
    }

    /**
     * Creates a new component instance.
     */
    public TestConnector() {
        super("test-connector-category", "test-connector-type");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration() {
    }

}
