package com.arassec.igor.core.application;

import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.core.util.ModelPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ServiceManager}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Service-Manager Tests")
class ServiceManagerTest {

    /**
     * The class under test.
     */
    private ServiceManager serviceManager;

    /**
     * Mocks the service repository.
     */
    @Mock
    private ServiceRepository serviceRepository;

    /**
     * A service for testing.
     */
    @Mock
    private Service serviceMock;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        serviceManager = new ServiceManager(serviceRepository);
    }

    /**
     * Tests saving.
     */
    @Test
    @DisplayName("Tests saving a service.")
    void testSave() {
        serviceManager.save(serviceMock);
        verify(serviceRepository, times(1)).upsert(eq(serviceMock));
    }

    /**
     * Tests loading.
     */
    @Test
    @DisplayName("Tests loading a service.")
    void testLoad() {
        when(serviceRepository.findById(eq("service-id"))).thenReturn(serviceMock);
        Service loadedService = serviceManager.load("service-id");
        assertEquals(serviceMock, loadedService);
    }

    /**
     * Tests loading a page of services.
     */
    @Test
    @DisplayName("Tests loading a page of services.")
    void testLoadPage() {
        ModelPage<Service> modelPage = new ModelPage<>();
        when(serviceRepository.findPage(eq(1), eq(5), eq("service-filter"))).thenReturn(modelPage);
        ModelPage<Service> loadedPage = serviceManager.loadPage(1, 5, "service-filter");
        assertEquals(modelPage, loadedPage);
    }

    /**
     * Tests loading by name.
     */
    @Test
    @DisplayName("Tests loading a service by its name.")
    void testLoadByName() {
        when(serviceRepository.findByName(eq("service-name"))).thenReturn(serviceMock);
        Service loadedService = serviceManager.loadByName("service-name");
        assertEquals(serviceMock, loadedService);
    }

    /**
     * Tests loading services of a specified category.
     */
    @Test
    @DisplayName("Tests loading services of a category.")
    void testLoadAllOfCategory() {
        Service first = mock(Service.class);
        when(first.getCategoryId()).thenReturn("a");
        Service second = mock(Service.class);
        when(second.getCategoryId()).thenReturn("b");
        when(second.getName()).thenReturn("z");
        Service third = mock(Service.class);
        when(third.getCategoryId()).thenReturn("b");
        when(third.getName()).thenReturn("y");

        when(serviceRepository.findAll()).thenReturn(List.of(first, second, third));

        ModelPage<Service> modelPage = serviceManager.loadAllOfCategory("b", 0, 5);

        assertEquals("y", modelPage.getItems().get(0).getName());
        assertEquals("z", modelPage.getItems().get(1).getName());
    }

    /**
     * Tests deleting a service.
     */
    @Test
    @DisplayName("Tests deleting a service.")
    void testDelete() {
        serviceManager.deleteService("service-id");
        verify(serviceRepository, times(1)).deleteById(eq("service-id"));
    }

    /**
     * Tests finding jobs that use a service.
     */
    @Test
    @DisplayName("Tests finding referencing jobs.")
    void testGetReferencingJobs() {
        serviceManager.getReferencingJobs("id", 1, 5);
        verify(serviceRepository, times(1)).findReferencingJobs(eq("id"), eq(1), eq(5));
    }

}
