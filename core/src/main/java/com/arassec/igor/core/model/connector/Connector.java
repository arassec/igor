package com.arassec.igor.core.model.connector;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorSimulationSafe;
import com.arassec.igor.core.util.validation.UniqueConnectorName;

/**
 * Defines a connector to an outside service. Connectors are the interface used by igor to communicate with other services.
 */
@UniqueConnectorName
public interface Connector extends IgorComponent {

    /**
     * Returns the connector's name.
     *
     * @return The connector's name.
     */
    @IgorSimulationSafe
    String getName();

    /**
     * Sets the connector's name.
     *
     * @param name The new connector name to set.
     */
    @IgorSimulationSafe
    void setName(String name);

    /**
     * Tests the connector's configuration.
     */
    void testConfiguration();

}
