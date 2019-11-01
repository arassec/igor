package com.arassec.igor.core.util;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.provider.Provider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ApplicationContextProvider} utility.
 */
@DisplayName("ApplicationContext-Provider Tests")
class ApplicationContextProviderTest {

    /**
     * Initializes the test environment.
     */
    @BeforeAll
    static void initialize() {
        ApplicationContext applicationContextMock = mock(ApplicationContext.class);

        when(applicationContextMock.getBean(eq(String.class))).thenReturn("test-string");

        Map<String, Action> beansOfType = new HashMap<>();
        Action firstActionMock = mock(Action.class);
        when(firstActionMock.getTypeId()).thenReturn("type-a");
        beansOfType.put("a", firstActionMock);
        Action secondActionMock = mock(Action.class);
        when(secondActionMock.getTypeId()).thenReturn("type-b");
        beansOfType.put("b", secondActionMock);

        when(applicationContextMock.getBeansOfType(eq(Action.class))).thenReturn(beansOfType);

        ApplicationContextProvider applicationContextProvider = new ApplicationContextProvider();
        applicationContextProvider.setApplicationContext(applicationContextMock);
    }

    /**
     * Tests getting a Spring-Bean from the context.
     */
    @Test
    @DisplayName("Tests getting a Spring-Bean.")
    void testGetBean() {
        assertNull(ApplicationContextProvider.getBean(Integer.class));
        assertEquals("test-string", ApplicationContextProvider.getBean(String.class));
    }

    /**
     * Tests getting an igor component from the context.
     */
    @Test
    @DisplayName("Tests getting an IgorComponent")
    void testGetIgorComponent() {
        assertThrows(IllegalStateException.class, () -> ApplicationContextProvider.getIgorComponent(Provider.class, "id"));
        assertEquals("type-b", ApplicationContextProvider.getIgorComponent(Action.class, "type-b").getTypeId());
    }

}
