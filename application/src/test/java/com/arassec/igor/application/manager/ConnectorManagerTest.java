package com.arassec.igor.application.manager;

import com.arassec.igor.application.util.IgorComponentUtil;
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
     * Mock-Utility for igor components.
     */
    @Mock
    private IgorComponentUtil igorComponentUtil;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        connectorManager = new ConnectorManager(connectorRepository, igorComponentUtil);
    }

    /**
     * Tests saving.
     */
    @Test
    @DisplayName("Tests saving a connector.")
    void testSave() {
        connectorManager.save(connectorMock);
        verify(connectorRepository, times(1)).upsert(connectorMock);
    }

    /**
     * Tests loading.
     */
    @Test
    @DisplayName("Tests loading a connector.")
    void testLoad() {
        when(connectorRepository.findById("connector-id")).thenReturn(connectorMock);
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
        when(connectorRepository.findPage(1, 5, "connector-filter")).thenReturn(modelPage);
        ModelPage<Connector> loadedPage = connectorManager.loadPage(1, 5, "connector-filter");
        assertEquals(modelPage, loadedPage);
    }

    /**
     * Tests loading by name.
     */
    @Test
    @DisplayName("Tests loading a connector by its name.")
    void testLoadByName() {
        when(connectorRepository.findByName("connector-name")).thenReturn(connectorMock);
        Connector loadedConnector = connectorManager.loadByName("connector-name");
        assertEquals(connectorMock, loadedConnector);
    }

    /**
     * Tests loading connectors of a specified type.
     */
    @Test
    @DisplayName("Tests loading connectors of a type.")
    void testLoadAllOfType() {
        Connector first = mock(Connector.class);
        when(igorComponentUtil.getTypeId(first)).thenReturn("a");
        Connector second = mock(Connector.class);
        when(igorComponentUtil.getTypeId(second)).thenReturn("b");
        when(second.getName()).thenReturn("z");
        Connector third = mock(Connector.class);
        when(igorComponentUtil.getTypeId(third)).thenReturn("b");
        when(third.getName()).thenReturn("y");

        when(connectorRepository.findAll()).thenReturn(List.of(first, second, third));

        ModelPage<Connector> modelPage = connectorManager.loadAllOfType(Set.of("b"), 0, 5);

        assertEquals("y", modelPage.getItems().get(0).getName());
        assertEquals("z", modelPage.getItems().get(1).getName());
    }

    /**
     * Tests loading connectors of a type without specifying the type.
     */
    @Test
    @DisplayName("Tests loading connectors of a type without specifying the type.")
    void testLoadAllOfTypeInputValidation() {
        ModelPage<Connector> modelPage = connectorManager.loadAllOfType(null, 1, 2);
        assertEquals(0, modelPage.getNumber());
        assertEquals(0, modelPage.getSize());
        assertEquals(0, modelPage.getItems().size());
        assertEquals(0, modelPage.getTotalPages());

        modelPage = connectorManager.loadAllOfType(Set.of(), 3, 4);
        assertEquals(0, modelPage.getNumber());
        assertEquals(0, modelPage.getSize());
        assertEquals(0, modelPage.getItems().size());
        assertEquals(0, modelPage.getTotalPages());
    }

    /**
     * Tests deleting a connector.
     */
    @Test
    @DisplayName("Tests deleting a connector.")
    void testDelete() {
        connectorManager.deleteConnector("connector-id");
        verify(connectorRepository, times(1)).deleteById("connector-id");
    }

    /**
     * Tests finding jobs that use a connector.
     */
    @Test
    @DisplayName("Tests finding referencing jobs.")
    void testGetReferencingJobs() {
        connectorManager.getReferencingJobs("id", 1, 5);
        verify(connectorRepository, times(1)).findReferencingJobs("id", 1, 5);
    }

}
