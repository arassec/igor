package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.persistence.dao.JobDao;
import com.arassec.igor.persistence.dao.JobServiceReferenceDao;
import com.arassec.igor.persistence.dao.ServiceDao;
import com.arassec.igor.persistence.entity.JobServiceReferenceEntity;
import com.arassec.igor.persistence.entity.JobServiceReferenceIdentity;
import com.arassec.igor.persistence.entity.ServiceEntity;
import com.arassec.igor.persistence.test.TestService;
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
 * Tests the {@link JdbcServiceRepository}.
 */
@DisplayName("Service-Repository tests")
class JdbcServiceRepositoryTest {

    /**
     * The repository under test.
     */
    private JdbcServiceRepository repository;

    /**
     * The DAO for services.
     */
    private ServiceDao serviceDao;

    /**
     * The DAO for jobs.
     */
    private JobDao jobDao;

    /**
     * DAO for job-service-references.
     */
    private JobServiceReferenceDao jobServiceReferenceDao;

    /**
     * ObjectMapper for JSON conversion.
     */
    private ObjectMapper persistenceServiceMapper;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        jobDao = mock(JobDao.class);
        serviceDao = mock(ServiceDao.class);
        jobServiceReferenceDao = mock(JobServiceReferenceDao.class);
        persistenceServiceMapper = mock(ObjectMapper.class);
        repository = new JdbcServiceRepository(serviceDao, jobDao, jobServiceReferenceDao, persistenceServiceMapper);
    }

    /**
     * Tests saving a new service.
     */
    @Test
    @DisplayName("Tests saving a new service.")
    @SneakyThrows(JsonProcessingException.class)
    void testUpsertNew() {
        Service service = new TestService();
        service.setName("service-name");

        when(persistenceServiceMapper.writeValueAsString(eq(service))).thenReturn("service-json");

        Service savedService = repository.upsert(service);

        assertNotNull(savedService.getId());

        ArgumentCaptor<ServiceEntity> argCap = ArgumentCaptor.forClass(ServiceEntity.class);
        verify(serviceDao, times(1)).save(argCap.capture());

        assertEquals(savedService.getId(), argCap.getValue().getId());
        assertEquals(service.getName(), argCap.getValue().getName());
        assertEquals("service-json", argCap.getValue().getContent());
    }

    /**
     * Tests saving an existing service.
     */
    @Test
    @DisplayName("Tests saving an existing service.")
    @SneakyThrows(JsonProcessingException.class)
    void testUpsertExisting() {
        Service service = new TestService();
        service.setId("service-id");
        service.setName("service-name");

        when(persistenceServiceMapper.writeValueAsString(eq(service))).thenReturn("service-json");

        // With Service-ID but without persisted service (i.e. upsert during import)
        when(serviceDao.findById(eq("service-id"))).thenReturn(Optional.empty());
        Service upsertedService = repository.upsert(service);

        assertEquals("service-id", upsertedService.getId());
        assertEquals("service-name", upsertedService.getName());

        ArgumentCaptor<ServiceEntity> argCap = ArgumentCaptor.forClass(ServiceEntity.class);
        verify(serviceDao, times(1)).save(argCap.capture());

        assertEquals("service-id", argCap.getValue().getId());
        assertEquals("service-name", argCap.getValue().getName());
        assertEquals("service-json", argCap.getValue().getContent());

        // "normal" upsert: provided ID and existing, persisted service:
        ServiceEntity serviceEntity = new ServiceEntity();
        when(serviceDao.findById(eq("service-id"))).thenReturn(Optional.of(serviceEntity));

        repository.upsert(service);

        verify(serviceDao, times(1)).save(eq(serviceEntity));
        assertEquals("service-json", serviceEntity.getContent());
    }

    /**
     * Tests finding a service by its ID.
     */
    @Test
    @DisplayName("Tests finding a service by its ID.")
    @SneakyThrows(JsonProcessingException.class)
    void testFindById() {
        when(serviceDao.findById(eq("service-id"))).thenReturn(Optional.empty());
        assertNull(repository.findById("service-id"));

        ServiceEntity serviceEntity = new ServiceEntity();
        serviceEntity.setContent("service-json");
        when(serviceDao.findById(eq("service-id"))).thenReturn(Optional.of(serviceEntity));

        Service service = new TestService();
        when(persistenceServiceMapper.readValue(eq("service-json"), eq(Service.class))).thenReturn(service);

        assertEquals(service, repository.findById("service-id"));
    }

    /**
     * Tests finding a service by its name.
     */
    @Test
    @DisplayName("Tests finding a service by its name.")
    @SneakyThrows(JsonProcessingException.class)
    void testFindByName() {
        assertNull(repository.findByName("service-name"));

        ServiceEntity serviceEntity = new ServiceEntity();
        serviceEntity.setContent("service-json");

        when(serviceDao.findByName(eq("service-name"))).thenReturn(serviceEntity);

        Service service = new TestService();
        when(persistenceServiceMapper.readValue(eq("service-json"), eq(Service.class))).thenReturn(service);

        assertEquals(service, repository.findByName("service-name"));
    }

    /**
     * Tests finding all services.
     */
    @Test
    @DisplayName("Tests finding all services.")
    @SneakyThrows(JsonProcessingException.class)
    void testFindAll() {
        assertTrue(repository.findAll().isEmpty());

        ServiceEntity serviceEntity = new ServiceEntity();
        serviceEntity.setContent("service-json");

        when(serviceDao.findAll()).thenReturn(List.of(serviceEntity));

        when(persistenceServiceMapper.readValue(eq("service-json"), eq(Service.class))).thenReturn(new TestService());

        assertEquals(1, repository.findAll().size());
        assertTrue(repository.findAll().get(0) instanceof TestService);
    }

    /**
     * Tests finding an empty page of services.
     */
    @Test
    @DisplayName("Tests finding an empty page of services.")
    void testFindEmptyPage() {
        ModelPage<Service> modelPage = repository.findPage(1, 2, null);
        assertEquals(1, modelPage.getNumber());
        assertEquals(2, modelPage.getSize());
        assertEquals(0, modelPage.getTotalPages());
        assertTrue(modelPage.getItems().isEmpty());
    }

    /**
     * Tests finding an empty page of services without a name filter.
     */
    @Test
    @DisplayName("Tests finding an empty page of services without a name filter.")
    @SneakyThrows(JsonProcessingException.class)
    void testFindPageNoNameFilter() {
        ServiceEntity serviceEntity = new ServiceEntity();
        serviceEntity.setContent("service-json");

        @SuppressWarnings("unchecked")
        Page<ServiceEntity> page = mock(Page.class);
        when(page.hasContent()).thenReturn(true);
        when(page.getNumber()).thenReturn(3);
        when(page.getSize()).thenReturn(4);
        when(page.getTotalPages()).thenReturn(5);
        when(page.getContent()).thenReturn(List.of(serviceEntity));

        ArgumentCaptor<Pageable> argCap = ArgumentCaptor.forClass(Pageable.class);
        when(serviceDao.findAll(argCap.capture())).thenReturn(page);

        when(persistenceServiceMapper.readValue(eq("service-json"), eq(Service.class))).thenReturn(new TestService());

        ModelPage<Service> resultPage = repository.findPage(1, 2, null);

        assertEquals(1, argCap.getValue().getPageNumber());
        assertEquals(2, argCap.getValue().getPageSize());
        assertEquals(Sort.by("name"), argCap.getValue().getSort());

        assertEquals(3, resultPage.getNumber());
        assertEquals(4, resultPage.getSize());
        assertEquals(5, resultPage.getTotalPages());
        assertEquals(1, resultPage.getItems().size());
        assertTrue(resultPage.getItems().get(0) instanceof TestService);
    }

    /**
     * Tests finding an empty page of services with a name filter.
     */
    @Test
    @DisplayName("Tests finding an empty page of services with a name filter.")
    @SneakyThrows(JsonProcessingException.class)
    void testFindPageWithNameFilter() {
        ServiceEntity serviceEntity = new ServiceEntity();
        serviceEntity.setContent("service-json");

        @SuppressWarnings("unchecked")
        Page<ServiceEntity> page = mock(Page.class);
        when(page.hasContent()).thenReturn(true);
        when(page.getNumber()).thenReturn(3);
        when(page.getSize()).thenReturn(4);
        when(page.getTotalPages()).thenReturn(5);
        when(page.getContent()).thenReturn(List.of(serviceEntity));

        ArgumentCaptor<Pageable> argCap = ArgumentCaptor.forClass(Pageable.class);
        when(serviceDao.findByNameContainingIgnoreCase(eq("name-filter"), argCap.capture())).thenReturn(page);

        when(persistenceServiceMapper.readValue(eq("service-json"), eq(Service.class))).thenReturn(new TestService());

        ModelPage<Service> resultPage = repository.findPage(1, 2, "name-filter");

        assertEquals(1, argCap.getValue().getPageNumber());
        assertEquals(2, argCap.getValue().getPageSize());
        assertEquals(Sort.by("name"), argCap.getValue().getSort());

        assertEquals(3, resultPage.getNumber());
        assertEquals(4, resultPage.getSize());
        assertEquals(5, resultPage.getTotalPages());
        assertEquals(1, resultPage.getItems().size());
        assertTrue(resultPage.getItems().get(0) instanceof TestService);
    }

    /**
     * Tests deleting a service.
     */
    @Test
    @DisplayName("Tests deleting a service.")
    void testDeleteById() {
        repository.deleteById("service-id");
        verify(serviceDao, times(1)).deleteById(eq("service-id"));
        verify(jobServiceReferenceDao, times(1)).deleteByServiceId(eq("service-id"));
    }

    /**
     * Tests finding jobs referenced by a service.
     */
    @Test
    @DisplayName("Tests finding jobs referenced by a service.")
    void testFindReferencingJobs() {
        assertNotNull(repository.findReferencingJobs(null, 1, 2));

        JobServiceReferenceEntity entity = new JobServiceReferenceEntity();
        entity.setJobServiceReferenceIdentity(new JobServiceReferenceIdentity("job-id", "service-id"));

        @SuppressWarnings("unchecked")
        Page<JobServiceReferenceEntity> page = mock(Page.class);
        when(page.hasContent()).thenReturn(true);
        when(page.get()).thenReturn(Stream.of(entity));

        when(jobServiceReferenceDao.findByServiceId(eq("service-id"), any(Pageable.class))).thenReturn(page);

        when(jobDao.findNameById(eq("job-id"))).thenReturn("job-name");

        ModelPage<Pair<String, String>> referencingJobs = repository.findReferencingJobs("service-id", 1, 2);

        assertEquals(1, referencingJobs.getItems().size());
        assertEquals("job-id", referencingJobs.getItems().get(0).getKey());
        assertEquals("job-name", referencingJobs.getItems().get(0).getValue());
    }

}