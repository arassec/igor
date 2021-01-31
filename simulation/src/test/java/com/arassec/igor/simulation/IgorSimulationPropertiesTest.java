package com.arassec.igor.simulation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link IgorSimulationProperties}.
 */
class IgorSimulationPropertiesTest {

    /**
     * Tests the simulation properties default values.
     */
    @Test
    @DisplayName("Tests the simulation properties default values.")
    void testSimulationPropertiesDefaults() {
        IgorSimulationProperties igorSimulationProperties = new IgorSimulationProperties();
        assertEquals(900, igorSimulationProperties.getTimeout());
    }

}
