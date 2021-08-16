package com.arassec.igor.simulation.job.proxy;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorSimulationSafe;
import com.arassec.igor.core.model.connector.BaseConnector;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.job.execution.JobExecution;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ConnectorProxy}.
 */
@DisplayName("Tests the Connector-Proxy.")
class ConnectorProxyTest {

    /**
     * Tests connector method invocations.
     */
    @Test
    @DisplayName("Tests invocation of methods of the proxy.")
    void testInvoke() {
        TestConnector testConnector = new TestConnector();
        testConnector.setName("test-connector");

        Connector connectorProxy = (Connector) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Connector.class},
                new ConnectorProxy(testConnector));

        assertEquals("test-connector", connectorProxy.getName());
        connectorProxy.setName("updated-connector-name");
        assertEquals("updated-connector-name", connectorProxy.getName());

        // The property should be false:
        Assertions.assertFalse(testConnector.isTestConfigurationInvoked());
        // The proxy should NOT call the real method due to the missing @IgorSimulationSafe annotation:
        connectorProxy.testConfiguration();
        Assertions.assertFalse(testConnector.isTestConfigurationInvoked());
        // Check, that the invokation does change the property:
        testConnector.testConfiguration();
        Assertions.assertTrue(testConnector.isTestConfigurationInvoked());
    }

    /**
     * Tests that the proxied connector's initialize method is called.
     */
    @Test
    @DisplayName("Tests that the proxied connector's initialize method is called.")
    void testInvokeInitilize() {
        TestConnector connectorMock = mock(TestConnector.class);

        Connector connectorProxy = (Connector) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Connector.class},
                new ConnectorProxy(connectorMock));

        JobExecution jobExecution = new JobExecution();

        connectorProxy.initialize(jobExecution);

        verify(connectorMock, times(1)).initialize(jobExecution);
    }

    /**
     * Tests that the proxied connector's shutdown method is called.
     */
    @Test
    @DisplayName("Tests that the proxied connector's shutdown method is called.")
    void testInvokeShutdown() {
        TestConnector connectorMock = mock(TestConnector.class);

        Connector connectorProxy = (Connector) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Connector.class},
                new ConnectorProxy(connectorMock));

        JobExecution jobExecution = new JobExecution();

        connectorProxy.shutdown(jobExecution);

        verify(connectorMock, times(1)).shutdown(jobExecution);
    }

    @Getter
    @Setter
    @IgorComponent(typeId = "simulation-test-type", categoryId = "simulation-test-category")
    private static class TestConnector extends BaseConnector implements Connector {

        /**
         * Property to support testing if the testConfiguration method has been invoked.
         */
        private boolean testConfigurationInvoked = false;

        /**
         * Throws an {@link IllegalStateException} to test proxying this connector.
         */
        @Override
        public void testConfiguration() {
            testConfigurationInvoked = true;
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unused")
        public Integer simulationSafeMethod() {
            return 666;
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unused")
        public String simulationUnsafeMethod() {
            return "this-should-not-be-returned-in-simulations";
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unused")
        @IgorSimulationSafe
        public String directlyAnnotatedSimulationSafeMethod() {
            return "real-value-from-connector";
        }

    }

}
