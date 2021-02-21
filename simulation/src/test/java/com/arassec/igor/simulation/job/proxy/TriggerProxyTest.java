package com.arassec.igor.simulation.job.proxy;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.trigger.Trigger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link TriggerProxy}.
 */
@DisplayName("Tests the Trigger-Proxy.")
class TriggerProxyTest {

    /**
     * Tests initializing the trigger through the proxy.
     */
    @Test
    @DisplayName("Tests initializing the provider through the proxy.")
    void testInitialize() {
        Trigger triggerMock = mock(Trigger.class);
        TriggerProxy triggerProxy = new TriggerProxy(triggerMock, 10);

        JobExecution jobExecution = new JobExecution();

        triggerProxy.initialize(jobExecution);

        verify(triggerMock, times(1)).initialize(jobExecution);

        assertEquals(10, triggerProxy.getSimulationLimit());
    }

    /**
     * Tests exception handling during proxy initialization.
     */
    @Test
    @DisplayName("Tests exception handling during proxy initialization.")
    void testInitializeFail() {
        TriggerProxy triggerProxy = new TriggerProxy(null, 10);
        triggerProxy.initialize(new JobExecution());
        assertNotNull(triggerProxy.getErrorCause());
    }

    /**
     * Tests creating the initial data item with the proxy.
     */
    @Test
    @DisplayName("Tests creating the initial data item with the proxy.")
    @SuppressWarnings("unchecked")
    void testCreateDataItem() {
        Map<String, Object> meta = new HashMap<>();
        meta.put(DataKey.SIMULATION.getKey(), false);

        Map<String, Object> dataItem = new HashMap<>();
        dataItem.put(DataKey.META.getKey(), meta);

        Trigger triggerMock = mock(Trigger.class);
        when(triggerMock.createDataItem()).thenReturn(dataItem);

        TriggerProxy triggerProxy = new TriggerProxy(triggerMock, 10);
        triggerProxy.initialize(JobExecution.builder().jobId("job-id").build());

        Map<String, Object> result = triggerProxy.createDataItem();
        assertTrue(((Boolean) ((Map<String, Object>) result.get(DataKey.META.getKey())).get(DataKey.SIMULATION.getKey())));
    }

}
