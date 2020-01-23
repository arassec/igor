package com.arassec.igor.web.simulation;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.execution.JobExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link BaseProxy} methods by using a {@link ActionProxy}.
 */
@DisplayName("Tests the Base-Proxy.")
class BaseProxyTest {

    /**
     * The proxy implementation to test the base class with.
     */
    private ActionProxy actionProxy;

    /**
     * The action mock to use for testing.
     */
    private Action actionMock;

    @BeforeEach
    void initialize() {
        actionMock = mock(Action.class);
        actionProxy = new ActionProxy(actionMock);
    }

    /**
     * Tests shutting down the proxy.
     */
    @Test
    @DisplayName("Tests shutting down the proxy.")
    void testShutdown() {
        JobExecution jobExecution = new JobExecution();
        actionProxy.shutdown("job-id", "task-id", jobExecution);
        verify(actionMock, times(1)).shutdown(eq("job-id"), eq("task-id"), eq(jobExecution));
    }

    /**
     * Tests error handling during shutdown.
     */
    @Test
    @DisplayName("Tests error handling during shutdown.")
    void testShutdownFail() {
        ActionProxy actionProxy = new ActionProxy(null);
        actionProxy.shutdown("job-id", "task-id", new JobExecution());
        assertNotNull(actionProxy.getErrorCause());
    }

    /**
     * Tests getters and setters of the proxy.
     */
    @Test
    @DisplayName("Tests getters and setters of the proxy.")
    void testGetUneditableProperties() {
        actionProxy.getUnEditableProperties();
        verify(actionMock, times(1)).getUnEditableProperties();

        actionProxy.getCategoryId();
        verify(actionMock, times(1)).getCategoryId();

        actionProxy.getTypeId();
        verify(actionMock, times(1)).getTypeId();

        actionProxy.getId();
        verify(actionMock, times(1)).getId();

        actionProxy.setId("id");
        verify(actionMock, times(1)).setId(eq("id"));
    }

}