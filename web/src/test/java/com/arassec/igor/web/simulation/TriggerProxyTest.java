package com.arassec.igor.web.simulation;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.trigger.EventTrigger;
import com.arassec.igor.core.model.trigger.Trigger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
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
        TriggerProxy triggerProxy = new TriggerProxy(triggerMock);

        JobExecution jobExecution = new JobExecution();

        triggerProxy.initialize("job-id", jobExecution);

        verify(triggerMock, times(1)).initialize(eq("job-id"), eq(jobExecution));
    }

    /**
     * Tests exception handling during proxy initialization.
     */
    @Test
    @DisplayName("Tests exception handling during proxy initialization.")
    void testInitializeFail() {
        TriggerProxy triggerProxy = new TriggerProxy(null);
        triggerProxy.initialize("job-id", new JobExecution());
        assertNotNull(triggerProxy.getErrorCause());
    }

    /**
     * Tests getting meta-data.
     */
    @Test
    @DisplayName("Tests getting meta-data.")
    void testGetMetaData() {
        Trigger triggerMock = mock(Trigger.class);
        when(triggerMock.getMetaData()).thenReturn(Map.of("meta", "data"));
        TriggerProxy triggerProxy = new TriggerProxy(triggerMock);
        Map<String, Object> metaData = triggerProxy.getMetaData();
        assertEquals(2, metaData.size());
        assertEquals(true, metaData.get(DataKey.SIMULATION.getKey()));
        assertEquals("data", metaData.get("meta"));
    }

    /**
     * Tests getting data.
     */
    @Test
    @DisplayName("Tests getting data.")
    void testGetData() {
        Trigger triggerMock = mock(Trigger.class);
        TriggerProxy triggerProxy = new TriggerProxy(triggerMock);
        triggerProxy.getData();
        verify(triggerMock, times(1)).getData();
    }

    /**
     * Tests getting data from an event trigger.
     */
    @Test
    @DisplayName("Tests getting data from an event trigger.")
    void testGetDataEventTrigger() {
        EventTrigger eventTriggerMock = mock(EventTrigger.class);
        TriggerProxy triggerProxy = new TriggerProxy(eventTriggerMock);
        triggerProxy.getData();
        verify(eventTriggerMock, times(1)).getSimulationData();
        verify(eventTriggerMock, times(0)).getData();
    }

}
