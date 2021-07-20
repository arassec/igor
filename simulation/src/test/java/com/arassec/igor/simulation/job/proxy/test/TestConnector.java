package com.arassec.igor.simulation.job.proxy.test;


import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorSimulationSafe;
import com.arassec.igor.core.model.connector.BaseConnector;

/**
 * A connector for testing.
 */
@IgorComponent(typeId = "test-connector-type", categoryId = "test-connector-category")
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
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration() {
    }

}
