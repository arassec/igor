package com.arassec.igor.core.application;

import com.arassec.igor.core.IgorCoreProperties;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.connector.BaseConnector;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.util.IgorException;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link IgorComponentRegistry}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IgorComponent-Registry Tests")
class IgorComponentRegistryTest {

    /**
     * The class under test.
     */
    private IgorComponentRegistry igorComponentRegistry;

    /**
     * Mock of the spring application context.
     */
    private ApplicationContext applicationContextMock;

    /**
     * An action mock.
     */
    @Mock
    private Action actionMock;

    /**
     * A provider mock.
     */
    @Mock
    private Provider providerMock;

    /**
     * A trigger mock.
     */
    @Mock
    private Trigger triggerMock;

    /**
     * Igor's core configuration properties.
     */
    @Mock
    private IgorCoreProperties igorCoreProperties;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        when(actionMock.getTypeId()).thenReturn("action-type-id");
        when(actionMock.getCategoryId()).thenReturn("action-category-id");

        when(providerMock.getTypeId()).thenReturn("provider-type-id");
        when(providerMock.getCategoryId()).thenReturn("provider-category-id");

        when(triggerMock.getTypeId()).thenReturn("trigger-type-id");
        when(triggerMock.getCategoryId()).thenReturn("trigger-category-id");

        applicationContextMock = mock(ApplicationContext.class);

        igorComponentRegistry = new IgorComponentRegistry(List.of(actionMock), List.of(providerMock), List.of(triggerMock),
                List.of(new TestConnectorImpl()), igorCoreProperties);
        igorComponentRegistry.afterPropertiesSet();
        igorComponentRegistry.setApplicationContext(applicationContextMock);
    }

    /**
     * Tests getting categories.
     */
    @Test
    @DisplayName("Tests getting all categories of a component type")
    void testGetCategoriesOfComponentType() {
        assertNotNull(new IgorComponentRegistry(List.of(), List.of(), List.of(), List.of(), null).getCategoriesOfComponentType(Action.class));

        Set<String> categoriesOfComponentType = igorComponentRegistry.getCategoriesOfComponentType(Action.class);
        assertEquals("action-category-id", categoriesOfComponentType.iterator().next());
    }

    /**
     * Tests getting types in a category.
     */
    @Test
    @DisplayName("Tests getting types of a category")
    void testGetTypesOfCategory() {
        assertTrue(igorComponentRegistry.getTypesOfCategory("unknown-category").isEmpty());
        Set<String> typesOfCategory = igorComponentRegistry.getTypesOfCategory("action-category-id");
        assertEquals("action-type-id", typesOfCategory.iterator().next());
    }

    /**
     * Tests creating a job instance as prototype.
     */
    @Test
    @DisplayName("Tests creating a job instance as prototype.")
    void testCreateJobPrototype() {
        doReturn(triggerMock).when(applicationContextMock).getBean(eq(triggerMock.getClass()));
        doReturn(providerMock).when(applicationContextMock).getBean(eq(providerMock.getClass()));

        Job jobPrototype = igorComponentRegistry.createJobPrototype();

        verify(triggerMock, times(1)).setId(anyString());
        verify(providerMock, times(1)).setId(anyString());
        assertEquals("New Job", jobPrototype.getName());
        assertTrue(jobPrototype.isActive());
    }

    /**
     * Tests creating a connector instance as prototype.
     */
    @Test
    @DisplayName("Tests creating a connector instance as prototype.")
    void testCreateActionPrototype() {
        doReturn(actionMock).when(applicationContextMock).getBean(eq(actionMock.getClass()));

        Action actionPrototype = igorComponentRegistry.createActionPrototype();

        assertNotNull(actionPrototype);
        verify(actionPrototype, times(1)).setId(anyString());
    }

    /**
     * Tests getting a component instance.
     */
    @Test
    @DisplayName("Tests getting an Action instance.")
    void testGetActionInstance() {
        assertThrows(IllegalArgumentException.class, () -> igorComponentRegistry.createActionInstance("unknown-type-id", null));
        igorComponentRegistry.createActionInstance("action-type-id", null);
        verify(applicationContextMock, times(1)).getBean(eq(actionMock.getClass()));
    }

    /**
     * Tests getting a component instance.
     */
    @Test
    @DisplayName("Tests getting a Connector instance.")
    void testGetConnectorInstance() {
        assertThrows(IllegalArgumentException.class, () -> igorComponentRegistry.createConnectorInstance("unknown-type-id", null));
        igorComponentRegistry.createConnectorInstance("connector-type-id", null);
        verify(applicationContextMock, times(1)).getBean(eq(TestConnectorImpl.class));
    }

    /**
     * Tests getting a connector instance with parameters that must be applied to the new instance.
     */
    @Test
    @DisplayName("Tests getting a Connector instance with parameters.")
    void testGetConnectorInstanceWithParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("testParam", 666);
        when(applicationContextMock.getBean(eq(TestConnectorImpl.class))).thenReturn(new TestConnectorImpl());
        TestConnectorImpl connectorInstance = (TestConnectorImpl) igorComponentRegistry.createConnectorInstance("connector-type-id", params);
        assertEquals(666, connectorInstance.getTestParam());
    }

    /**
     * Tests getting a component instance.
     */
    @Test
    @DisplayName("Tests getting a Provider instance.")
    void testGetProviderInstance() {
        assertThrows(IllegalArgumentException.class, () -> igorComponentRegistry.createProviderInstance("unknown-type-id", null));
        igorComponentRegistry.createProviderInstance("provider-type-id", null);
        verify(applicationContextMock, times(1)).getBean(eq(providerMock.getClass()));
    }

    /**
     * Tests getting a component instance.
     */
    @Test
    @DisplayName("Tests getting a Trigger instance.")
    void testGetTriggerInstance() {
        assertThrows(IllegalArgumentException.class, () -> igorComponentRegistry.createTriggerInstance("unknown-type-id", null));
        igorComponentRegistry.createTriggerInstance("trigger-type-id", null);
        verify(applicationContextMock, times(1)).getBean(eq(triggerMock.getClass()));
    }

    /**
     * Tests getting all component candidates for a parameter class.
     */
    @Test
    @DisplayName("Tests getting all component candidates for a parameter class.")
    void testGetParameterCategoryAndType() {
        assertTrue(igorComponentRegistry.getConnectorParameterCategoryAndType(String.class).isEmpty());

        TestConnectorImpl expected = new TestConnectorImpl();

        // The interface works...
        Map<String, Set<String>> candidates = igorComponentRegistry.getConnectorParameterCategoryAndType(TestConnector.class);
        assertEquals(1, candidates.size());
        assertEquals(1, candidates.get(expected.getCategoryId()).size());
        assertEquals(expected.getTypeId(), candidates.get(expected.getCategoryId()).iterator().next());

        // ...as well as the concrete implementation.
        candidates = igorComponentRegistry.getConnectorParameterCategoryAndType(TestConnectorImpl.class);
        assertEquals(1, candidates.size());
        assertEquals(1, candidates.get(expected.getCategoryId()).size());
        assertEquals(expected.getTypeId(), candidates.get(expected.getCategoryId()).iterator().next());
    }

    /**
     * Connector-Interface for testing.
     */
    private interface TestConnector extends Connector {
    }

    /**
     * Connector-Implementation for testing.
     */
    private static class TestConnectorImpl extends BaseConnector implements TestConnector {

        @Getter
        @IgorParam
        @SuppressWarnings("FieldMayBeFinal")
        private int testParam = 0;

        /**
         * Creates a new component instance.
         */
        TestConnectorImpl() {
            super("connector-category-id", "connector-type-id");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void testConfiguration() throws IgorException {
        }

    }

}
