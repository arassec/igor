package com.arassec.igor.web.simulation;

import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.web.test.TestConnector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link ConnectorProxy}.
 */
@DisplayName("Tests the Connector-Proxy.")
class ConnectorProxyTest {

    /**
     * Tests connector method invocations.
     */
    @Test
    void testInvoke() {
        TestConnector testConnector = new TestConnector();
        testConnector.setName("test-connector");

        Connector connectorProxy = (Connector) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Connector.class},
                new ConnectorProxy(testConnector));

        assertEquals("test-connector", connectorProxy.getName());
        connectorProxy.setName("updated-connector-name");
        assertEquals("updated-connector-name", connectorProxy.getName());

        // The property should be false:
        assertFalse(testConnector.isTestConfigurationInvoked());
        // The proxy should NOT call the real method due to the missing @IgorSimulationSafe annotation:
        connectorProxy.testConfiguration();
        assertFalse(testConnector.isTestConfigurationInvoked());
        // Check, that the invokation does change the property:
        testConnector.testConfiguration();
        assertTrue(testConnector.isTestConfigurationInvoked());
    }

}