package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.persistence.dao.ConnectorDao;
import com.arassec.igor.persistence.dao.JobConnectorReferenceDao;
import com.arassec.igor.persistence.entity.ConnectorEntity;
import com.arassec.igor.persistence.entity.JobConnectorReferenceView;
import com.arassec.igor.persistence.test.TestConnector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link JdbcConnectorRepository}.
 */
@DisplayName("Connector-Repository tests")
class JdbcConnectorRepositoryTest {

    /**
     * The repository under test.
     */
    private JdbcConnectorRepository repository;

    /**
     * The DAO for connectors.
     */
    private ConnectorDao connectorDao;

    /**
     * DAO for job-connector-references.
     */
    private JobConnectorReferenceDao jobConnectorReferenceDao;

    /**
     * ObjectMapper for JSON conversion.
     */
    private ObjectMapper persistenceConnectorMapper;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        connectorDao = mock(ConnectorDao.class);
        jobConnectorReferenceDao = mock(JobConnectorReferenceDao.class);
        persistenceConnectorMapper = mock(ObjectMapper.class);
        repository = new JdbcConnectorRepository(connectorDao, jobConnectorReferenceDao, persistenceConnectorMapper);
    }

    /**
     * Tests saving a new connector.
     */
    @Test
    @DisplayName("Tests saving a new connector.")
    @SneakyThrows(JsonProcessingException.class)
    void testUpsertNew() {
        Connector connector = new TestConnector();
        connector.setName("connector-name");

        when(persistenceConnectorMapper.writeValueAsString(connector)).thenReturn("connector-json");

        Connector savedConnector = repository.upsert(connector);

        assertNotNull(savedConnector.getId());

        ArgumentCaptor<ConnectorEntity> argCap = ArgumentCaptor.forClass(ConnectorEntity.class);
        verify(connectorDao, times(1)).save(argCap.capture());

        assertEquals(savedConnector.getId(), argCap.getValue().getId());
        assertEquals(connector.getName(), argCap.getValue().getName());
        assertEquals("connector-json", argCap.getValue().getContent());
    }

    /**
     * Tests saving an existing connector.
     */
    @Test
    @DisplayName("Tests saving an existing connector.")
    @SneakyThrows(JsonProcessingException.class)
    void testUpsertExisting() {
        Connector connector = new TestConnector();
        connector.setId("connector-id");
        connector.setName("connector-name");

        when(persistenceConnectorMapper.writeValueAsString(connector)).thenReturn("connector-json");

        // With Connector-ID but without persisted connector (i.e. upsert during import)
        when(connectorDao.findById("connector-id")).thenReturn(Optional.empty());
        Connector upsertedConnector = repository.upsert(connector);

        assertEquals("connector-id", upsertedConnector.getId());
        assertEquals("connector-name", upsertedConnector.getName());

        ArgumentCaptor<ConnectorEntity> argCap = ArgumentCaptor.forClass(ConnectorEntity.class);
        verify(connectorDao, times(1)).save(argCap.capture());

        assertEquals("connector-id", argCap.getValue().getId());
        assertEquals("connector-name", argCap.getValue().getName());
        assertEquals("connector-json", argCap.getValue().getContent());

        // "normal" upsert: provided ID and existing, persisted connector:
        ConnectorEntity connectorEntity = new ConnectorEntity();
        when(connectorDao.findById("connector-id")).thenReturn(Optional.of(connectorEntity));

        repository.upsert(connector);

        verify(connectorDao, times(1)).save(connectorEntity);
        assertEquals("connector-json", connectorEntity.getContent());
    }

    /**
     * Tests finding a connector by its ID.
     */
    @Test
    @DisplayName("Tests finding a connector by its ID.")
    @SneakyThrows(JsonProcessingException.class)
    void testFindById() {
        when(connectorDao.findById("connector-id")).thenReturn(Optional.empty());
        assertNull(repository.findById("connector-id"));

        ConnectorEntity connectorEntity = new ConnectorEntity();
        connectorEntity.setContent("connector-json");
        when(connectorDao.findById("connector-id")).thenReturn(Optional.of(connectorEntity));

        Connector connector = new TestConnector();
        when(persistenceConnectorMapper.readValue("connector-json", Connector.class)).thenReturn(connector);

        assertEquals(connector, repository.findById("connector-id"));
    }

    /**
     * Tests finding a connector by its name.
     */
    @Test
    @DisplayName("Tests finding a connector by its name.")
    @SneakyThrows(JsonProcessingException.class)
    void testFindByName() {
        assertNull(repository.findByName("connector-name"));

        ConnectorEntity connectorEntity = new ConnectorEntity();
        connectorEntity.setContent("connector-json");

        when(connectorDao.findByName("connector-name")).thenReturn(connectorEntity);

        Connector connector = new TestConnector();
        when(persistenceConnectorMapper.readValue("connector-json", Connector.class)).thenReturn(connector);

        assertEquals(connector, repository.findByName("connector-name"));
    }

    /**
     * Tests finding all connectors.
     */
    @Test
    @DisplayName("Tests finding all connectors.")
    @SneakyThrows(JsonProcessingException.class)
    void testFindAll() {
        assertTrue(repository.findAll().isEmpty());

        ConnectorEntity connectorEntity = new ConnectorEntity();
        connectorEntity.setContent("connector-json");

        when(connectorDao.findAll()).thenReturn(List.of(connectorEntity));

        when(persistenceConnectorMapper.readValue("connector-json", Connector.class)).thenReturn(new TestConnector());

        assertEquals(1, repository.findAll().size());
        assertTrue(repository.findAll().get(0) instanceof TestConnector);
    }

    /**
     * Tests finding an empty page of connectors.
     */
    @Test
    @DisplayName("Tests finding an empty page of connectors.")
    void testFindEmptyPage() {
        ModelPage<Connector> modelPage = repository.findPage(1, 2, null);
        assertEquals(1, modelPage.getNumber());
        assertEquals(2, modelPage.getSize());
        assertEquals(0, modelPage.getTotalPages());
        assertTrue(modelPage.getItems().isEmpty());
    }

    /**
     * Tests finding an empty page of connectors without a name filter.
     */
    @Test
    @DisplayName("Tests finding an empty page of connectors without a name filter.")
    @SneakyThrows(JsonProcessingException.class)
    void testFindPageNoNameFilter() {
        ConnectorEntity connectorEntity = new ConnectorEntity();
        connectorEntity.setContent("connector-json");

        @SuppressWarnings("unchecked")
        Page<ConnectorEntity> page = mock(Page.class);
        when(page.hasContent()).thenReturn(true);
        when(page.getNumber()).thenReturn(3);
        when(page.getSize()).thenReturn(4);
        when(page.getTotalPages()).thenReturn(5);
        when(page.getContent()).thenReturn(List.of(connectorEntity));

        ArgumentCaptor<Pageable> argCap = ArgumentCaptor.forClass(Pageable.class);
        when(connectorDao.findAll(argCap.capture())).thenReturn(page);

        when(persistenceConnectorMapper.readValue("connector-json", Connector.class)).thenReturn(new TestConnector());

        ModelPage<Connector> resultPage = repository.findPage(1, 2, null);

        assertEquals(1, argCap.getValue().getPageNumber());
        assertEquals(2, argCap.getValue().getPageSize());
        assertEquals(Sort.by("name"), argCap.getValue().getSort());

        assertEquals(3, resultPage.getNumber());
        assertEquals(4, resultPage.getSize());
        assertEquals(5, resultPage.getTotalPages());
        assertEquals(1, resultPage.getItems().size());
        assertTrue(resultPage.getItems().get(0) instanceof TestConnector);
    }

    /**
     * Tests finding an empty page of connectors with a name filter.
     */
    @Test
    @DisplayName("Tests finding an empty page of connectors with a name filter.")
    @SneakyThrows(JsonProcessingException.class)
    void testFindPageWithNameFilter() {
        ConnectorEntity connectorEntity = new ConnectorEntity();
        connectorEntity.setContent("connector-json");

        @SuppressWarnings("unchecked")
        Page<ConnectorEntity> page = mock(Page.class);
        when(page.hasContent()).thenReturn(true);
        when(page.getNumber()).thenReturn(3);
        when(page.getSize()).thenReturn(4);
        when(page.getTotalPages()).thenReturn(5);
        when(page.getContent()).thenReturn(List.of(connectorEntity));

        ArgumentCaptor<Pageable> argCap = ArgumentCaptor.forClass(Pageable.class);
        when(connectorDao.findByNameContainingIgnoreCase(eq("name-filter"), argCap.capture())).thenReturn(page);

        when(persistenceConnectorMapper.readValue("connector-json", Connector.class)).thenReturn(new TestConnector());

        ModelPage<Connector> resultPage = repository.findPage(1, 2, "name-filter");

        assertEquals(1, argCap.getValue().getPageNumber());
        assertEquals(2, argCap.getValue().getPageSize());
        assertEquals(Sort.by("name"), argCap.getValue().getSort());

        assertEquals(3, resultPage.getNumber());
        assertEquals(4, resultPage.getSize());
        assertEquals(5, resultPage.getTotalPages());
        assertEquals(1, resultPage.getItems().size());
        assertTrue(resultPage.getItems().get(0) instanceof TestConnector);
    }

    /**
     * Tests deleting a connector.
     */
    @Test
    @DisplayName("Tests deleting a connector.")
    void testDeleteById() {
        repository.deleteById("connector-id");
        verify(connectorDao, times(1)).deleteById("connector-id");
        verify(jobConnectorReferenceDao, times(1)).deleteByConnectorId("connector-id");
    }

    /**
     * Tests finding jobs referenced by a connector.
     */
    @Test
    @DisplayName("Tests finding jobs referenced by a connector.")
    void testFindReferencingJobs() {
        assertNotNull(repository.findReferencingJobs(null, 1, 2));

        JobConnectorReferenceView entity = mock(JobConnectorReferenceView.class);
        when(entity.getJobId()).thenReturn("job-id");
        when(entity.getJobName()).thenReturn("job-name");

        @SuppressWarnings("unchecked")
        Page<JobConnectorReferenceView> page = mock(Page.class);
        when(page.hasContent()).thenReturn(true);
        when(page.get()).thenReturn(Stream.of(entity));

        when(jobConnectorReferenceDao.findByConnectorId(eq("connector-id"), any(Pageable.class))).thenReturn(page);

        ModelPage<Pair<String, String>> referencingJobs = repository.findReferencingJobs("connector-id", 1, 2);

        assertEquals(1, referencingJobs.getItems().size());
        assertEquals("job-id", referencingJobs.getItems().get(0).getKey());
        assertEquals("job-name", referencingJobs.getItems().get(0).getValue());
    }

}