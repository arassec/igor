package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.persistence.dao.ConnectorDao;
import com.arassec.igor.persistence.dao.JobConnectorReferenceDao;
import com.arassec.igor.persistence.dao.JobDao;
import com.arassec.igor.persistence.entity.JobConnectorReferenceEntity;
import com.arassec.igor.persistence.entity.JobConnectorReferenceIdentity;
import com.arassec.igor.persistence.entity.JobEntity;
import com.arassec.igor.persistence.test.TestAction;
import com.arassec.igor.persistence.test.TestConnector;
import com.arassec.igor.persistence.test.TestProvider;
import com.arassec.igor.persistence.test.TestTrigger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link JdbcJobRepository}.
 */
@DisplayName("Job-Repository tests")
class JdbcJobRepositoryTest {

    /**
     * The repository under test.
     */
    private JdbcJobRepository repository;

    /**
     * DAO for job entities.
     */
    private JobDao jobDao;

    /**
     * DAO for connector entities.
     */
    private ConnectorDao connectorDao;

    /**
     * DAO for job-connector-references.
     */
    private JobConnectorReferenceDao jobConnectorReferenceDao;

    /**
     * ObjectMapper for JSON conversion.
     */
    private ObjectMapper persistenceJobMapper;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        jobDao = mock(JobDao.class);
        connectorDao = mock(ConnectorDao.class);
        jobConnectorReferenceDao = mock(JobConnectorReferenceDao.class);
        persistenceJobMapper = mock(ObjectMapper.class);
        repository = new JdbcJobRepository(jobDao, connectorDao, jobConnectorReferenceDao, persistenceJobMapper);
    }

    /**
     * Tests upserting a new entity.
     */
    @Test
    @DisplayName("Tests upserting a new entity.")
    @SneakyThrows(JsonProcessingException.class)
    void testUpsertNew() {
        Job job = new Job();
        job.setName("job-name");
        job.setTrigger(new TestTrigger());
        job.setProvider(new TestProvider());
        TestAction testAction = new TestAction();
        testAction.setTestConnector(new TestConnector());
        testAction.getTestConnector().setId("connector-id");
        job.getActions().add(testAction);

        when(persistenceJobMapper.writeValueAsString(any(Job.class))).thenReturn("job-json");

        Job savedJob = repository.upsert(job);

        assertNotNull(savedJob.getId());
        assertNotNull(savedJob.getTrigger().getId());
        assertNotNull(savedJob.getProvider().getId());
        assertNotNull(savedJob.getActions().get(0).getId());

        ArgumentCaptor<JobEntity> argCap = ArgumentCaptor.forClass(JobEntity.class);
        verify(jobDao, times(1)).save(argCap.capture());
        assertEquals("job-json", argCap.getValue().getContent());

        verify(jobConnectorReferenceDao, times(1)).deleteByJobId(eq(savedJob.getId()));

        ArgumentCaptor<JobConnectorReferenceEntity> argCapTwo = ArgumentCaptor.forClass(JobConnectorReferenceEntity.class);
        verify(jobConnectorReferenceDao, times(1)).save(argCapTwo.capture());

        assertEquals(savedJob.getId(), argCapTwo.getValue().getJobConnectorReferenceIdentity().getJobId());
        assertEquals("connector-id", argCapTwo.getValue().getJobConnectorReferenceIdentity().getConnectorId());
    }

    /**
     * Tests saving an existing entity.
     */
    @Test
    @DisplayName("Tests saving an existing entity.")
    @SneakyThrows(JsonProcessingException.class)
    void testUpsertExisting() {
        Job job = new Job();
        job.setId("job-id");
        job.setName("job-name");

        when(persistenceJobMapper.writeValueAsString(eq(job))).thenReturn("job-json");

        // With Job-ID but without persisted job (i.e. upsert during import)
        when(jobDao.findById(eq("job-id"))).thenReturn(Optional.empty());

        Job upsertedJob = repository.upsert(job);

        ArgumentCaptor<JobEntity> argCap = ArgumentCaptor.forClass(JobEntity.class);
        verify(jobDao, times(1)).save(argCap.capture());

        assertEquals("job-id", argCap.getValue().getId());
        assertEquals(job.getName(), argCap.getValue().getName());
        assertEquals("job-json", argCap.getValue().getContent());

        assertEquals("job-id", upsertedJob.getId());
        assertEquals(job.getName(), upsertedJob.getName());

        // "normal" upsert: provided ID and existing, persisted job:
        when(jobDao.findById(eq("job-id"))).thenReturn(Optional.of(new JobEntity()));

        repository.upsert(job);

        argCap = ArgumentCaptor.forClass(JobEntity.class);
        verify(jobDao, times(2)).save(argCap.capture());

        assertEquals(job.getName(), argCap.getValue().getName());
        assertEquals("job-json", argCap.getValue().getContent());
    }

    /**
     * Tests finding a job by its ID.
     */
    @Test
    @DisplayName("Tests finding a job by its ID.")
    @SneakyThrows(JsonProcessingException.class)
    void testFindById() {
        when(jobDao.findById(eq("job-id"))).thenReturn(Optional.empty());
        assertNull(repository.findById("job-id"));

        JobEntity jobEntity = new JobEntity();
        jobEntity.setContent("job-json");

        when(jobDao.findById(eq("job-id"))).thenReturn(Optional.of(jobEntity));

        Job job = new Job();
        when(persistenceJobMapper.readValue(eq("job-json"), eq(Job.class))).thenReturn(job);

        Job foundJob = repository.findById("job-id");
        assertEquals(job, foundJob);
    }

    /**
     * Tests finding a job by its name.
     */
    @Test
    @DisplayName("Tests finding a job by its name.")
    @SneakyThrows(JsonProcessingException.class)
    void testFindByName() {
        assertNull(repository.findByName(null));
        assertNull(repository.findByName("job-id"));

        JobEntity jobEntity = new JobEntity();
        jobEntity.setContent("job-json");
        when(jobDao.findByName(eq("job-name"))).thenReturn(jobEntity);

        Job job = new Job();
        when(persistenceJobMapper.readValue(eq("job-json"), eq(Job.class))).thenReturn(job);

        Job foundJob = repository.findByName("job-name");
        assertEquals(job, foundJob);
    }

    /**
     * Tests reading all jobs.
     */
    @Test
    @DisplayName("Tests reading all jobs.")
    @SneakyThrows(JsonProcessingException.class)
    void testFindAll() {
        assertTrue(repository.findAll().isEmpty());

        List<JobEntity> entityList = List.of(new JobEntity(), new JobEntity());
        entityList.forEach(entity -> entity.setContent("job-json"));
        when(jobDao.findAll()).thenReturn(entityList);
        when(persistenceJobMapper.readValue(eq("job-json"), eq(Job.class))).thenReturn(new Job());

        List<Job> allJobs = repository.findAll();
        assertEquals(2, allJobs.size());
    }

    /**
     * Tests finding a page of jobs.
     */
    @Test
    @DisplayName("Tests finding a page of jobs.")
    @SneakyThrows(JsonProcessingException.class)
    void testFindPage() {
        ModelPage<Job> page = repository.findPage(1, 2, null);
        assertEquals(1, page.getNumber());
        assertEquals(2, page.getSize());
        assertEquals(0, page.getTotalPages());
        assertNotNull(page.getItems());

        @SuppressWarnings("unchecked")
        Page<JobEntity> entityPage = mock(Page.class);
        when(entityPage.hasContent()).thenReturn(true);
        when(entityPage.getNumber()).thenReturn(123);
        when(entityPage.getSize()).thenReturn(456);
        when(entityPage.getTotalPages()).thenReturn(789);

        List<JobEntity> entityList = List.of(new JobEntity(), new JobEntity());
        entityList.forEach(entity -> entity.setContent("job-json"));
        when(entityPage.getContent()).thenReturn(entityList);

        when(persistenceJobMapper.readValue(eq("job-json"), eq(Job.class))).thenReturn(new Job());

        when(jobDao.findAll(any(Pageable.class))).thenReturn(entityPage);

        page = repository.findPage(1, 2, null);

        assertEquals(123, page.getNumber());
        assertEquals(456, page.getSize());
        assertEquals(789, page.getTotalPages());
        assertEquals(2, page.getItems().size());
    }

    /**
     * Tests finding a page of jobs with a name filter.
     */
    @Test
    @DisplayName("Tests finding a page of jobs with a name filter.")
    void testFindPageWithNameFilter() {
        repository.findPage(1, 2, "name-filter");
        verify(jobDao, times(1)).findByNameContainingIgnoreCase(eq("name-filter"), any(Pageable.class));
    }

    /**
     * Tests deleting a job by its ID.
     */
    @Test
    @DisplayName("Tests deleting a job by its ID.")
    void testDeleteById() {
        repository.deleteById("job-id");
        verify(jobDao, times(1)).deleteById(eq("job-id"));
        verify(jobConnectorReferenceDao, times(1)).deleteByJobId(eq("job-id"));
    }

    /**
     * Tests finding the connectors referenced by a job.
     */
    @Test
    @DisplayName("Tests finding the connectors referenced by a job.")
    void testFindReferencedConnectors() {
        assertTrue(repository.findReferencedConnectors(null).isEmpty());

        JobConnectorReferenceEntity entity = new JobConnectorReferenceEntity();
        entity.setJobConnectorReferenceIdentity(new JobConnectorReferenceIdentity("job-id", "connector-id"));

        when(jobConnectorReferenceDao.findByJobId(eq("job-id"))).thenReturn(List.of(entity));

        when(connectorDao.findNameById(eq("connector-id"))).thenReturn("connector-name");

        Set<Pair<String, String>> referencedConnectors = repository.findReferencedConnectors("job-id");

        assertEquals(1, referencedConnectors.size());
        assertEquals("connector-id", referencedConnectors.iterator().next().getKey());
        assertEquals("connector-name", referencedConnectors.iterator().next().getValue());
    }

}