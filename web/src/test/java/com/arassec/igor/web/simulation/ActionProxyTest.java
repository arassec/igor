package com.arassec.igor.web.simulation;

import com.arassec.igor.core.model.action.Action;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        actionProxy.initialize();
        verify(actionMock, times(1)).initialize();
    }

}
