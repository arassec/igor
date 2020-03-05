package com.arassec.igor.web.simulation;

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
        ActionProxy actionProxy = new ActionProxy(actionMock);
        actionProxy.initialize("1", "2", new JobExecution());
        verify(actionMock, times(1)).initialize(eq("1"), eq("2"), any(JobExecution.class));
    }

    /**
     * Tests processing of data with the proxy.
     */
    @Test
    @DisplayName("Tests processing an action with the proxy.")
    void testProcess() {
        Action actionMock = mock(Action.class);
        ActionProxy actionProxy = new ActionProxy(actionMock);

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
     * Tests exception handling during processing.
     */
    @Test
    @DisplayName("Tests exception handling during processing.")
    void testProcessFail() {
        ActionProxy actionProxy = new ActionProxy(null);
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
        ActionProxy actionProxy = new ActionProxy(actionMock);

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
        ActionProxy actionProxy = new ActionProxy(null);
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
        ActionProxy actionProxy = new ActionProxy(actionMock);
        actionProxy.getNumThreads();
        verify(actionMock, times(1)).getNumThreads();
    }

    /**
     * Tests checking the action activity through the proxy.
     */
    @Test
    @DisplayName("Tests checking the action activity through the proxy.")
    void testIsActive() {
        Action actionMock = mock(Action.class);
        ActionProxy actionProxy = new ActionProxy(actionMock);
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
        ActionProxy actionProxy = new ActionProxy(actionMock);
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
        ActionProxy actionProxy = new ActionProxy(actionMock);

        actionProxy.setName("test-action-name");

        verify(actionMock, times(1)).setName(eq("test-action-name"));

        actionProxy.getName();

        verify(actionMock, times(1)).getName();
    }

}
