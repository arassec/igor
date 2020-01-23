package com.arassec.igor.web.simulation;

import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.web.test.TestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ServiceProxy}.
 */
@DisplayName("Tests the Service-Proxy.")
class ServiceProxyTest {

    /**
     * Tests service method invocations.
     */
    @Test
    void testInvoke() {
        TestService testService = new TestService();
        testService.setName("test-service");

        Service serviceProxy = (Service) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Service.class},
                new ServiceProxy(testService));

        assertEquals("test-service", serviceProxy.getName());
        serviceProxy.setName("updated-service-name");
        assertEquals("updated-service-name", serviceProxy.getName());

        // The property should be false:
        assertFalse(testService.isTestConfigurationInvoked());
        // The proxy should NOT call the real method due to the missing @IgorSimulationSafe annotation:
        serviceProxy.testConfiguration();
        assertFalse(testService.isTestConfigurationInvoked());
        // Check, that the invokation does change the property:
        testService.testConfiguration();
        assertTrue(testService.isTestConfigurationInvoked());
    }

}