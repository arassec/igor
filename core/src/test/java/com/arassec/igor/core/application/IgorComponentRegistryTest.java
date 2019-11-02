package com.arassec.igor.core.application;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.service.BaseService;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.service.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        Action actionMock = mock(Action.class);
        when(actionMock.getTypeId()).thenReturn("action-type-id");
        when(actionMock.getCategoryId()).thenReturn("action-category-id");
        igorComponentRegistry = new IgorComponentRegistry(List.of(actionMock), List.of(), List.of(), List.of(new TestServiceImpl()));
        igorComponentRegistry.afterPropertiesSet();
    }

    /**
     * Tests getting categories.
     */
    @Test
    @DisplayName("Tests getting all categories of a component type")
    void testGetCategoriesOfComponentType() {
        assertTrue(igorComponentRegistry.getCategoriesOfComponentType(Provider.class).isEmpty());
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
    @DisplayName("Tests getting an IgorComponent instance.")
    void testGetInstance() {
        assertTrue(igorComponentRegistry.getInstance("unknown-type-id").isEmpty());
        Optional<IgorComponent> igorComponentOptional = igorComponentRegistry.getInstance("action-type-id");
        assertTrue(igorComponentOptional.isPresent());
        assertTrue(igorComponentOptional.get() instanceof Action);
        assertEquals("action-category-id", igorComponentOptional.get().getCategoryId());
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
         * {@inheritDoc}
         */
        @Override
        public void testConfiguration() throws ServiceException {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getCategoryId() {
            return "service-category-id";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getTypeId() {
            return "service-type-id";
        }
    }

}
