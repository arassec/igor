package com.arassec.igor.simulation.job.proxy;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.execution.JobExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ActionProxy}.
 */
@DisplayName("Tests the Action-Proxy.")
class ActionProxyTest {

    /**
     * Tests initializing the proxy.
     */
    @Test
    @DisplayName("Tests that initialization is passed to the delegate.")
    void testInitialize() {
        Action actionMock = mock(Action.class);
        ActionProxy actionProxy = new ActionProxy(actionMock, 10);
        actionProxy.initialize("1",  new JobExecution());
        verify(actionMock, times(1)).initialize(eq("1"), any(JobExecution.class));
        assertEquals(10, actionProxy.getSimulationLimit());
    }

    /**
     * Tests processing of data with the proxy.
     */
    @Test
    @DisplayName("Tests processing an action with the proxy.")
    void testProcess() {
        Action actionMock = mock(Action.class);
        ActionProxy actionProxy = new ActionProxy(actionMock, 10);

        // Assert an empty list if no data is processed:
        assertTrue(actionProxy.process(new HashMap<>(), new JobExecution()).isEmpty());

        // Normal processing: the delegate's data is returned:
        List<Map<String, Object>> data = List.of(Map.of("test", "output"));
        JobExecution jobExecution = new JobExecution();

        Map<String, Object> inputData = new HashMap<>();
        inputData.put("test", "input");
        inputData.put(DataKey.SIMULATION_LOG.getKey(), "simulation-log");

        when(actionMock.process(eq(inputData), eq(jobExecution))).thenReturn(data);

        List<Map<String, Object>> resultData = actionProxy.process(inputData, jobExecution);

        assertEquals(data, resultData);
        assertEquals(actionProxy.getCollectedData(), resultData);

        // simulation-log must be removed from the input data!
        assertFalse(inputData.containsKey(DataKey.SIMULATION_LOG.getKey()));
    }

    /**
     * Tests the simulation limit configuration.
     */
    @Test
    @DisplayName("Tests the simulation limit configuration.")
    void testSimulationLimit() {
        Action actionMock = mock(Action.class);
        ActionProxy actionProxy = new ActionProxy(actionMock, 1);

        Map<String, Object> data = new HashMap<>();

        JobExecution jobExecution = new JobExecution();

        when(actionMock.process(anyMap(), eq(jobExecution))).thenReturn(List.of(data, data, data));

        List<Map<String, Object>> resultData = actionProxy.process(data, jobExecution);
        assertEquals(1, resultData.size());

        resultData = actionProxy.process(data, jobExecution);
        assertEquals(0, resultData.size());
    }

    /**
     * Tests exception handling during processing.
     */
    @Test
    @DisplayName("Tests exception handling during processing.")
    void testProcessFail() {
        ActionProxy actionProxy = new ActionProxy(null, 10);
        assertTrue(actionProxy.process(null, null).isEmpty());
        assertNotNull(actionProxy.getErrorCause());
    }

    /**
     * Tests completing an action with the proxy.
     */
    @Test
    @DisplayName("Tests completing an action with the proxy.")
    void testComplete() {
        Action actionMock = mock(Action.class);
        ActionProxy actionProxy = new ActionProxy(actionMock, 10);

        // Assert an empty list if no data is processed:
        assertTrue(actionProxy.complete().isEmpty());

        // Normal case: the result is passed through:
        List<Map<String, Object>> data = List.of(Map.of("test", "output"));
        when(actionMock.complete()).thenReturn(data);

        List<Map<String, Object>> resultData = actionProxy.complete();

        assertEquals(data, resultData);
        assertEquals(actionProxy.getCollectedData(), resultData);
    }

    /**
     * Tests exception handling during completion.
     */
    @Test
    @DisplayName("Tests exception handling during completion.")
    void testCompleteFail() {
        ActionProxy actionProxy = new ActionProxy(null, 10);
        assertTrue(actionProxy.complete().isEmpty());
        assertNotNull(actionProxy.getErrorCause());
    }

    /**
     * Tests getting the number of threads from the proxy.
     */
    @Test
    @DisplayName("Tests getting the number of threads from the proxy.")
    void testGetNumThreads() {
        Action actionMock = mock(Action.class);
        ActionProxy actionProxy = new ActionProxy(actionMock, 10);
        actionProxy.getNumThreads();
        verify(actionMock, times(1)).getNumThreads();
    }

    /**
     * Tests setting the number of threads from the proxy.
     */
    @Test
    @DisplayName("Tests setting the number of threads from the proxy.")
    void testSetNumThreads() {
        Action actionMock = mock(Action.class);
        ActionProxy actionProxy = new ActionProxy(actionMock, 10);
        actionProxy.setNumThreads(23);
        verify(actionMock, times(1)).setNumThreads(eq(23));
    }

    /**
     * Tests checking the action activity through the proxy.
     */
    @Test
    @DisplayName("Tests checking the action activity through the proxy.")
    void testIsActive() {
        Action actionMock = mock(Action.class);
        ActionProxy actionProxy = new ActionProxy(actionMock, 10);
        actionProxy.isActive();
        verify(actionMock, times(1)).isActive();
    }

    /**
     * Tests setting the action's activity through the proxy.
     */
    @Test
    @DisplayName("Tests setting the action's activity through the proxy.")
    void testSetActive() {
        Action actionMock = mock(Action.class);
        ActionProxy actionProxy = new ActionProxy(actionMock, 10);
        actionProxy.setActive(true);
        verify(actionMock, times(1)).setActive(eq(true));
    }

    /**
     * Tests getting and setting the action's name.
     */
    @Test
    @DisplayName("Tests getting and setting the action's name.")
    void testName() {
        Action actionMock = mock(Action.class);
        ActionProxy actionProxy = new ActionProxy(actionMock, 10);

        actionProxy.setName("test-action-name");

        verify(actionMock, times(1)).setName(eq("test-action-name"));

        actionProxy.getName();

        verify(actionMock, times(1)).getName();
    }

    /**
     * Tests setting the description.
     */
    @Test
    @DisplayName("Tests setting the description.")
    void testSetDescription() {
        Action actionMock = mock(Action.class);
        ActionProxy actionProxy = new ActionProxy(actionMock, 10);

        actionProxy.setDescription("test-description");

        verify(actionMock, times(1)).setDescription(eq("test-description"));
    }

    /**
     * Tests setting the description.
     */
    @Test
    @DisplayName("Tests setting the description.")
    void testGetDescription() {
        Action actionMock = mock(Action.class);
        when(actionMock.getDescription()).thenReturn("test-description");

        ActionProxy actionProxy = new ActionProxy(actionMock, 10);

        assertEquals("test-description", actionProxy.getDescription());
    }

}
