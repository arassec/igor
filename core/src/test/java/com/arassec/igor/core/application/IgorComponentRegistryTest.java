package com.arassec.igor.core.application;

import com.arassec.igor.core.IgorCoreProperties;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.action.MissingComponentAction;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.connector.BaseConnector;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.connector.MissingComponentConnector;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.trigger.MissingComponentTrigger;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.util.IgorException;
import lombok.Getter;
import lombok.Setter;
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
     * A trigger mock.
     */
    @Mock
    private Trigger triggerMock;

    /**
     * A connector mock.
     */
    @Mock
    private Connector connectorMock;

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

        when(triggerMock.getTypeId()).thenReturn("trigger-type-id");
        when(triggerMock.getCategoryId()).thenReturn("trigger-category-id");

        when(connectorMock.getTypeId()).thenReturn("connector-type-id");
        when(connectorMock.getCategoryId()).thenReturn("connector-category-id");

        applicationContextMock = mock(ApplicationContext.class);

        igorComponentRegistry = new IgorComponentRegistry(List.of(actionMock, new TestAction()), List.of(triggerMock),
                List.of(new TestConnectorImpl(), connectorMock), igorCoreProperties);
        igorComponentRegistry.afterPropertiesSet();
        igorComponentRegistry.setApplicationContext(applicationContextMock);
    }

    /**
     * Tests getting categories.
     */
    @Test
    @DisplayName("Tests getting all categories of a component type")
    void testGetCategoriesOfComponentType() {
        assertNotNull(new IgorComponentRegistry(List.of(), List.of(), List.of(), null).getCategoriesOfComponentType(Action.class));

        Set<String> categoriesOfComponentType = igorComponentRegistry.getCategoriesOfComponentType(Action.class);
        assertEquals("action-category-id", categoriesOfComponentType.iterator().next());
    }

    /**
     * Tests getting action types in a category.
     */
    @Test
    @DisplayName("Tests getting action types of a category")
    void testGetActionTypesOfCategory() {
        assertTrue(igorComponentRegistry.getActionTypesOfCategory("unknown-category").isEmpty());
        Set<String> typesOfCategory = igorComponentRegistry.getActionTypesOfCategory("action-category-id");
        assertEquals("action-type-id", typesOfCategory.iterator().next());
    }

    /**
     * Tests getting trigger types in a category.
     */
    @Test
    @DisplayName("Tests getting trigger types of a category")
    void testGetTriggerTypesOfCategory() {
        assertTrue(igorComponentRegistry.getTriggerTypesOfCategory("unknown-category").isEmpty());
        Set<String> typesOfCategory = igorComponentRegistry.getTriggerTypesOfCategory("trigger-category-id");
        assertEquals("trigger-type-id", typesOfCategory.iterator().next());
    }

    /**
     * Tests getting connector types in a category.
     */
    @Test
    @DisplayName("Tests getting connector types of a category")
    void testGetConnectorTypesOfCategory() {
        assertTrue(igorComponentRegistry.getConnectorTypesOfCategory("unknown-category").isEmpty());
        Set<String> typesOfCategory = igorComponentRegistry.getConnectorTypesOfCategory("connector-category-id");
        assertEquals("connector-type-id", typesOfCategory.iterator().next());
    }

    /**
     * Tests creating a job instance as prototype.
     */
    @Test
    @DisplayName("Tests creating a job instance as prototype.")
    void testCreateJobPrototype() {
        doReturn(triggerMock).when(applicationContextMock).getBean(eq(triggerMock.getClass()));

        Job jobPrototype = igorComponentRegistry.createJobPrototype();

        verify(triggerMock, times(1)).setId(anyString());
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
        Action actionInstance = igorComponentRegistry.createActionInstance("unknown-type-id", null);
        assertTrue(actionInstance instanceof MissingComponentAction);

        igorComponentRegistry.createActionInstance("action-type-id", null);
        verify(applicationContextMock, times(1)).getBean(eq(actionMock.getClass()));
    }

    /**
     * Tests getting a component instance.
     */
    @Test
    @DisplayName("Tests getting a Connector instance.")
    void testGetConnectorInstance() {
        Connector connectorInstance = igorComponentRegistry.createConnectorInstance("unknown-type-id", null);
        assertTrue(connectorInstance instanceof MissingComponentConnector);

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
        params.put("intParam", 666);
        when(applicationContextMock.getBean(eq(TestConnectorImpl.class))).thenReturn(new TestConnectorImpl());
        TestConnectorImpl connectorInstance = (TestConnectorImpl) igorComponentRegistry.createConnectorInstance("connector-type-id", params);
        assertEquals(666, connectorInstance.getIntParam());
    }

    /**
     * Tests getting a component instance.
     */
    @Test
    @DisplayName("Tests getting a Trigger instance.")
    void testGetTriggerInstance() {
        Trigger triggerInstance = igorComponentRegistry.createTriggerInstance("unknown-type-id", null);
        assertTrue(triggerInstance instanceof MissingComponentTrigger);

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
     * Tests handling of missing connectors.
     */
    @Test
    @DisplayName("Tests handling of missing connectors.")
    void testHandleMissingConnectors() {
        when(applicationContextMock.getBean(eq(TestAction.class))).thenReturn(new TestAction());

        Map<String, Object> parameters = Map.of("missingConnector", new MissingComponentConnector("component-registry-test"));

        TestAction action = (TestAction) igorComponentRegistry.createActionInstance("missing-connector-action-type", parameters);

        Connector missingConnector = action.getMissingConnector();

        assertEquals("Missing Connector!", missingConnector.getName());
        assertEquals("Missing Connector!", missingConnector.toString());
    }

    /**
     * Tests setting the default value on new instances.
     */
    @Test
    @DisplayName("Tests setting the default value on new instances.")
    void testDefaultValueHandling() {
        when(applicationContextMock.getBean(eq(TestConnectorImpl.class))).thenReturn(new TestConnectorImpl());

        TestConnectorImpl connector = (TestConnectorImpl) igorComponentRegistry.createConnectorInstance("connector-type-id",
                null);

        assertNull(connector.getEmptyDefaultValueParam());
        assertTrue(connector.isBooleanParam());
        assertTrue(connector.getBooleanObjectParam());
        assertEquals(-128, connector.getByteParam());
        assertEquals(127, connector.getByteObjectParam().byteValue());
        assertEquals(123, connector.getShortParam());
        assertEquals(456, connector.getShortObjectParam().shortValue());
        assertEquals(23, connector.getIntParam());
        assertEquals(42, connector.getIntegerParam());
        assertEquals(123456789, connector.getLongParam());
        assertEquals(987654321, connector.getLongObjectParam());
        assertEquals(1.23f, connector.getFloatParam());
        assertEquals(4.56f, connector.getFloatObjectParam());
        assertEquals(1.23456789d, connector.getDoubleParam());
        assertEquals(9.87654321d, connector.getDoubleObjectParam());
        assertEquals('A', connector.getCharParam());
        assertEquals('B', connector.getCharacterParam());
        assertEquals("igor", connector.getStringParam());
    }

    /**
     * Connector-Interface for testing.
     */
    private interface TestConnector extends Connector {
    }

    /**
     * Connector-Implementation for testing.
     */
    @Getter
    @Setter
    private static class TestConnectorImpl extends BaseConnector implements TestConnector {

        /**
         * A parameter without a default value.
         */
        @IgorParam
        private String emptyDefaultValueParam;

        /**
         * boolean test parameter.
         */
        @IgorParam(defaultValue = "true")
        private boolean booleanParam;

        /**
         * Boolean test parameter.
         */
        @IgorParam(defaultValue = "true")
        private Boolean booleanObjectParam;

        /**
         * byte test parameter.
         */
        @IgorParam(defaultValue = "-128")
        private byte byteParam;

        /**
         * Byte test parameter.
         */
        @IgorParam(defaultValue = "127")
        private Byte byteObjectParam;

        /**
         * short test parameter.
         */
        @IgorParam(defaultValue = "123")
        private short shortParam;

        /**
         * Short test parameter.
         */
        @IgorParam(defaultValue = "456")
        private Short shortObjectParam;

        /**
         * int test parameter.
         */
        @IgorParam(defaultValue = "23")
        private int intParam;

        /**
         * Integer test parameter.
         */
        @IgorParam(defaultValue = "42")
        private Integer integerParam;

        /**
         * long test parameter.
         */
        @IgorParam(defaultValue = "123456789")
        private long longParam;

        /**
         * Long test parameter.
         */
        @IgorParam(defaultValue = "987654321")
        private Long longObjectParam;

        /**
         * float test parameter.
         */
        @IgorParam(defaultValue = "1.23")
        private float floatParam;

        /**
         * Float test parameter.
         */
        @IgorParam(defaultValue = "4.56")
        private Float floatObjectParam;

        /**
         * double test parameter.
         */
        @IgorParam(defaultValue = "1.23456789")
        private double doubleParam;

        /**
         * Double test parameter.
         */
        @IgorParam(defaultValue = "9.87654321")
        private Double doubleObjectParam;

        /**
         * char test parameter.
         */
        @IgorParam(defaultValue = "A")
        private char charParam;

        /**
         * Character test parameter.
         */
        @IgorParam(defaultValue = "B")
        private Character characterParam;

        /**
         * String test parameter.
         */
        @IgorParam(defaultValue = "igor")
        private String stringParam;

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

    /**
     * Action-Implementation for testing handling of missing connectors.
     */
    @Getter
    @Setter
    private static class TestAction extends BaseAction {

        /**
         * Parameter to test handling of missing connectors.
         */
        @IgorParam
        private Connector missingConnector;

        /**
         * Creates a new component instance.
         */
        protected TestAction() {
            super("missing-connector-action-category", "missing-connector-action-type");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
            return List.of();
        }
    }

}
