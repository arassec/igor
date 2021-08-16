package com.arassec.igor.core.model.connector;

import com.arassec.igor.core.util.IgorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the {@link MissingComponentConnector}.
 */
@DisplayName("Missing-Component-Connector tests")
class MissingComponentConnectorTest {

    /**
     * Tests creation of the component.
     */
    @Test
    @DisplayName("Tests creation of the component.")
    void testCreation() {
        MissingComponentConnector component = new MissingComponentConnector("unit-test-error-cause");
        assertEquals("unit-test-error-cause", component.getErrorCause());
    }

    /**
     * Tests that the configuration check always fails for this connector.
     */
    @Test
    @DisplayName("Tests that the configuration check always fails for this connector.")
    void testTestConfigurationAlwaysFail() {
        MissingComponentConnector component = new MissingComponentConnector("unit-test-error-cause");
        assertThrows(IgorException.class, component::testConfiguration);
    }

}
