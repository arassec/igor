package com.arassec.igor.core.application;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.service.BaseService;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.core.model.trigger.Trigger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.List;
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
    private Action actionMock = mock(Action.class);

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {

        when(actionMock.getTypeId()).thenReturn("action-type-id");
        when(actionMock.getCategoryId()).thenReturn("action-category-id");

        Provider providerMock = mock(Provider.class);
        when(providerMock.getTypeId()).thenReturn("provider-type-id");
        when(providerMock.getCategoryId()).thenReturn("provider-category-id");

        Trigger triggerMock = mock(Trigger.class);
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
     * Tests getting the category for a specific service interface.
     */
    @Test
    @DisplayName("Tests getting the category of a specific service interface.")
    void testGetCagetoryOfServiceInterface() {
        assertThrows(IllegalArgumentException.class, () -> igorComponentRegistry.getCagetoryOfServiceInterface(String.class));
        assertEquals("service-category-id", igorComponentRegistry.getCagetoryOfServiceInterface(TestService.class));
    }

    /**
     * Tests getting a component instance.
     */
    @Test
    @DisplayName("Tests getting an Action instance.")
    void testGetActionInstance() {
        assertThrows(IllegalArgumentException.class, () -> igorComponentRegistry.getActionInstance("unknown-type-id", null));
        igorComponentRegistry.getActionInstance("action-type-id", null);
        verify(applicationContextMock, times(1)).getBean(eq(actionMock.getClass()));
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
        public void testConfiguration() throws ServiceException {
        }

    }

}
