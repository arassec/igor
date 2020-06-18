package com.arassec.igor.web.simulation;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.provider.Provider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ProviderProxy}.
 */
@DisplayName("Tests the Provider-Proxy.")
class ProviderProxyTest {

    /**
     * Tests initializing the provider through the proxy.
     */
    @Test
    @DisplayName("Tests initializing the provider through the proxy.")
    void testInitialize() {
        Provider providerMock = mock(Provider.class);
        ProviderProxy providerProxy = new ProviderProxy(providerMock);

        JobExecution jobExecution = new JobExecution();

        providerProxy.initialize("job-id", jobExecution);

        verify(providerMock, times(1)).initialize(eq("job-id"), eq(jobExecution));
    }

    /**
     * Tests exception handling during proxy initialization.
     */
    @Test
    @DisplayName("Tests exception handling during proxy initialization.")
    void testInitializeFail() {
        ProviderProxy providerProxy = new ProviderProxy(null);
        providerProxy.initialize("job-id", new JobExecution());
        assertNotNull(providerProxy.getErrorCause());
    }

    /**
     * Tests checking for new data through the proxy.
     */
    @Test
    @DisplayName("Tests checking for new data through the proxy.")
    void testHasNext() {
        Provider providerMock = mock(Provider.class);
        ProviderProxy providerProxy = new ProviderProxy(providerMock);

        when(providerMock.getSimulationLimit()).thenReturn(2);
        when(providerMock.hasNext()).thenReturn(true);

        assertTrue(providerProxy.hasNext());
        assertTrue(providerProxy.hasNext());
        assertFalse(providerProxy.hasNext());
    }

    /**
     * Tests exception handling when {@link Provider#hasNext()} is called.
     */
    @Test
    @DisplayName("Tests exception handling during hasNex() method invocation.")
    void testHasNextFail() {
        ProviderProxy providerProxy = new ProviderProxy(null);
        assertFalse(providerProxy.hasNext());
        assertNotNull(providerProxy.getErrorCause());
    }

    /**
     * Tests retrieving the next data to process.
     */
    @Test
    @DisplayName("Tests retrieving the next data to process.")
    void testNext() {
        Provider providerMock = mock(Provider.class);
        ProviderProxy providerProxy = new ProviderProxy(providerMock);

        when(providerMock.next()).thenReturn(new HashMap<>());

        Map<String, Object> data = providerProxy.next();

        assertEquals(true, data.get(DataKey.SIMULATION.getKey()));
        assertEquals(data, providerProxy.getCollectedData().get(0));
    }

    /**
     * Tests error handling when retrieving the next data item.
     */
    @Test
    @DisplayName("Tests error handling when retrieving the next data item.")
    void testNextFail() {
        ProviderProxy providerProxy = new ProviderProxy(null);
        assertNull(providerProxy.next());
        assertNotNull(providerProxy.getErrorCause());
    }

    /**
     * Tests delegation of the simulation limit.
     */
    @Test
    @DisplayName("Tests delegation of the simulation limit.")
    void testSimulationLimit() {
        Provider providerMock = mock(Provider.class);
        ProviderProxy providerProxy = new ProviderProxy(providerMock);
        providerProxy.setSimulationLimit(666);
        providerProxy.getSimulationLimit();
        verify(providerMock, times(1)).setSimulationLimit(eq(666));
        verify(providerMock, times(1)).getSimulationLimit();
    }

}