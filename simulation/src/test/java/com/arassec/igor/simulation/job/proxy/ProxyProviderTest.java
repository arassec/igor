package com.arassec.igor.simulation.job.proxy;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.trigger.EventTrigger;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.simulation.job.proxy.test.TestAction;
import com.arassec.igor.simulation.job.proxy.test.TestConnector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link ProxyProvider}.
 */
@DisplayName("Tests the Proxy-Provider.")
class ProxyProviderTest {

    /**
     * The class under test.
     */
    private final ProxyProvider proxyProvider = new ProxyProvider();

    /**
     * Tests setting the Spring application context.
     */
    @Test
    @DisplayName("Tests setting the Spring application context.")
    void testSetAppliationContext() {
        ApplicationContext applicationContextMock = mock(ApplicationContext.class);
        proxyProvider.setApplicationContext(applicationContextMock);
        assertEquals(applicationContextMock, ReflectionTestUtils.getField(proxyProvider, "applicationContext"));
    }

    /**
     * Tests applying proxies to a job.
     */
    @Test
    @DisplayName("Tests applying proxies to a job.")
    void testApplyProxies() {
        ApplicationContext applicationContextMock = mock(ApplicationContext.class);
        proxyProvider.setApplicationContext(applicationContextMock);

        TestConnector testConnector = new TestConnector();
        TestAction testAction = new TestAction();
        testAction.setTestConnector(testConnector);

        Job job = Job.builder()
                .trigger(mock(Trigger.class))
                .actions(List.of(testAction))
                .build();

        proxyProvider.applyProxies(job);

        assertInstanceOf(TriggerProxy.class, job.getTrigger());
        assertInstanceOf(ActionProxy.class, job.getActions().getFirst());

        ActionProxy actionProxy = (ActionProxy) job.getActions().getFirst();

        TestAction testActionDelegate = (TestAction) actionProxy.getDelegate();

        assertTrue(testActionDelegate.getTestConnector().simulationSafe());
        assertNull(testActionDelegate.getTestConnector().notSimulationSafe());
    }

    /**
     * Tests applying proxies to a job with an event trigger.
     */
    @Test
    @DisplayName("Tests applying proxies to a job with an event trigger.")
    void testApplyProxiesEventTrigger() {
        ApplicationContext applicationContextMock = mock(ApplicationContext.class);
        proxyProvider.setApplicationContext(applicationContextMock);

        Job job = Job.builder()
                .trigger(mock(EventTrigger.class))
                .build();

        proxyProvider.applyProxies(job);

        assertInstanceOf(EventTriggerProxy.class, job.getTrigger());
    }

}
