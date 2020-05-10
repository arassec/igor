package com.arassec.igor.core.model.connector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link BaseConnector} model.
 */
@DisplayName("Base-Connector Tests")
class BaseConnectorTest {

    /**
     * The class under test.
     */
    private final BaseConnector baseConnector = mock(BaseConnector.class, CALLS_REAL_METHODS);

    /**
     * Tests the base-connector's name handling.
     */
    @Test
    @DisplayName("Tests name handling.")
    void testNameHandling() {
        baseConnector.setName("test-connector");
        assertEquals("test-connector", baseConnector.getName());
    }

}
