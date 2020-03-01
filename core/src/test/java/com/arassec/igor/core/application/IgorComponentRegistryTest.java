package com.arassec.igor.core.application;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.service.BaseService;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.core.model.trigger.Trigger;
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
                List.of(new TestServiceImpl()));
        igorComponentRegistry.afterPropertiesSet();
        igorComponentRegistry.setApplicationContext(applicationContextMock);
    }

    /**
     * Tests getting categories.
     */
    @Test
    @DisplayName("Tests getting all categories of a component type")
    void testGetCategoriesOfComponentType() {
        assertNotNull(new IgorComponentRegistry(List.of(), List.of(), List.of(), List.of()).getCategoriesOfComponentType(Action.class));

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
    @DisplayName("Tests getting a Service instance.")
    void testGetServiceInstance() {
        assertThrows(IllegalArgumentException.class, () -> igorComponentRegistry.createServiceInstance("unknown-type-id", null));
        igorComponentRegistry.createServiceInstance("service-type-id", null);
        verify(applicationContextMock, times(1)).getBean(eq(TestServiceImpl.class));
    }

    /**
     * Tests getting a service instance with parameters that must be applied to the new instance.
     */
    @Test
    @DisplayName("Tests getting a Service instance with parameters.")
    void testGetServiceInstanceWithParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("testParam", 666);
        when(applicationContextMock.getBean(eq(TestServiceImpl.class))).thenReturn(new TestServiceImpl());
        TestServiceImpl serviceInstance = (TestServiceImpl) igorComponentRegistry.createServiceInstance("service-type-id", params);
        assertEquals(666, serviceInstance.getTestParam());
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
        assertTrue(igorComponentRegistry.getParameterCategoryAndType(String.class).isEmpty());

        TestServiceImpl expected = new TestServiceImpl();

        // The interface works...
        Map<String, Set<String>> candidates = igorComponentRegistry.getParameterCategoryAndType(TestService.class);
        assertEquals(1, candidates.size());
        assertEquals(1, candidates.get(expected.getCategoryId()).size());
        assertEquals(expected.getTypeId(), candidates.get(expected.getCategoryId()).iterator().next());

        // ...as well as the concrete implementation.
        candidates = igorComponentRegistry.getParameterCategoryAndType(TestServiceImpl.class);
        assertEquals(1, candidates.size());
        assertEquals(1, candidates.get(expected.getCategoryId()).size());
        assertEquals(expected.getTypeId(), candidates.get(expected.getCategoryId()).iterator().next());
    }

    /**
     * Service-Interface for testing.
     */
    private interface TestService extends Service {
    }

    /**
     * Service-Implementation for testing.
     */
    private static class TestServiceImpl extends BaseService implements TestService {

        @Getter
        @IgorParam
        private int testParam = 0;

        /**
         * Creates a new component instance.
         */
        TestServiceImpl() {
            super("service-category-id", "service-type-id");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void testConfiguration() throws IgorException {
        }

    }

}
