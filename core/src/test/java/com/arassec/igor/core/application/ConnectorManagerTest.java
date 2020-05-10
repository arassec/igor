package com.arassec.igor.core.application;

import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.repository.ConnectorRepository;
import com.arassec.igor.core.util.ModelPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ConnectorManager}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Connector-Manager Tests")
class ConnectorManagerTest {

    /**
     * The class under test.
     */
    private ConnectorManager connectorManager;

    /**
     * Mocks the connector repository.
     */
    @Mock
    private ConnectorRepository connectorRepository;

    /**
     * A connector for testing.
     */
    @Mock
    private Connector connectorMock;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        connectorManager = new ConnectorManager(connectorRepository);
    }

    /**
     * Tests saving.
     */
    @Test
    @DisplayName("Tests saving a connector.")
    void testSave() {
        connectorManager.save(connectorMock);
        verify(connectorRepository, times(1)).upsert(eq(connectorMock));
    }

    /**
     * Tests loading.
     */
    @Test
    @DisplayName("Tests loading a connector.")
    void testLoad() {
        when(connectorRepository.findById(eq("connector-id"))).thenReturn(connectorMock);
        Connector loadedConnector = connectorManager.load("connector-id");
        assertEquals(connectorMock, loadedConnector);
    }

    /**
     * Tests loading a page of connectors.
     */
    @Test
    @DisplayName("Tests loading a page of connectors.")
    void testLoadPage() {
        ModelPage<Connector> modelPage = new ModelPage<>();
        when(connectorRepository.findPage(eq(1), eq(5), eq("connector-filter"))).thenReturn(modelPage);
        ModelPage<Connector> loadedPage = connectorManager.loadPage(1, 5, "connector-filter");
        assertEquals(modelPage, loadedPage);
    }

    /**
     * Tests loading by name.
     */
    @Test
    @DisplayName("Tests loading a connector by its name.")
    void testLoadByName() {
        when(connectorRepository.findByName(eq("connector-name"))).thenReturn(connectorMock);
        Connector loadedConnector = connectorManager.loadByName("connector-name");
        assertEquals(connectorMock, loadedConnector);
    }

    /**
     * Tests loading connectors of a specified category.
     */
    @Test
    @DisplayName("Tests loading connectors of a category.")
    void testLoadAllOfCategory() {
        Connector first = mock(Connector.class);
        when(first.getTypeId()).thenReturn("a");
        Connector second = mock(Connector.class);
        when(second.getTypeId()).thenReturn("b");
        when(second.getName()).thenReturn("z");
        Connector third = mock(Connector.class);
        when(third.getTypeId()).thenReturn("b");
        when(third.getName()).thenReturn("y");

        when(connectorRepository.findAll()).thenReturn(List.of(first, second, third));

        ModelPage<Connector> modelPage = connectorManager.loadAllOfType(Set.of("b"), 0, 5);

        assertEquals("y", modelPage.getItems().get(0).getName());
        assertEquals("z", modelPage.getItems().get(1).getName());
    }

    /**
     * Tests deleting a connector.
     */
    @Test
    @DisplayName("Tests deleting a connector.")
    void testDelete() {
        connectorManager.deleteConnector("connector-id");
        verify(connectorRepository, times(1)).deleteById(eq("connector-id"));
    }

    /**
     * Tests finding jobs that use a connector.
     */
    @Test
    @DisplayName("Tests finding referencing jobs.")
    void testGetReferencingJobs() {
        connectorManager.getReferencingJobs("id", 1, 5);
        verify(connectorRepository, times(1)).findReferencingJobs(eq("id"), eq(1), eq(5));
    }

}
