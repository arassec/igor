package com.arassec.igor.application.simulation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link SimulationResult}.
 */
class SimulationResultTest {

    /**
     * Tests initialization of the bean.
     */
    @Test
    @DisplayName("Tests initialization of the bean.")
    void testInitialize() {
        SimulationResult simulationResult = new SimulationResult();
        assertTrue(simulationResult.getResults().isEmpty());
        assertNull(simulationResult.getErrorCause());

        simulationResult = new SimulationResult(List.of(Map.of("a", "b")), "test-error-cause");
        assertEquals(Map.of("a", "b"), simulationResult.getResults().get(0));
        assertEquals("test-error-cause", simulationResult.getErrorCause());
    }

}
