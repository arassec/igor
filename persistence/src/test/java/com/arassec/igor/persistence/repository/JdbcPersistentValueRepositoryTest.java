package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.model.job.misc.PersistentValue;
import com.arassec.igor.persistence.dao.PersistentValueDao;
import com.arassec.igor.persistence.entity.PersistentValueEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link JdbcPersistentValueRepository}.
 */
@DisplayName("Persistent-Value-Repository tests.")
class JdbcPersistentValueRepositoryTest {

    /**
     * The repository under test.
     */
    private JdbcPersistentValueRepository repository;

    /**
     * DAO for Access to persistent values.
     */
    private PersistentValueDao persistentValueDao;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        persistentValueDao = mock(PersistentValueDao.class);
        repository = new JdbcPersistentValueRepository(persistentValueDao);
    }

    /**
     * Tests upserting a new entity.
     */
    @Test
    @DisplayName("Tests upserting a new entity.")
    void testUpsertNew() {
        PersistentValue value = new PersistentValue();
        value.setContent("persistent-value-content");

        PersistentValueEntity entity = new PersistentValueEntity();
        entity.setId(666L);
        entity.setCreated(Instant.now());

        when(persistentValueDao.save(any(PersistentValueEntity.class))).thenReturn(entity);

        PersistentValue persistedValue = repository.upsert("job-id", value);

        assertEquals(entity.getId(), persistedValue.getId());
        assertNotNull(persistedValue.getCreated());

        ArgumentCaptor<PersistentValueEntity> argCap = ArgumentCaptor.forClass(PersistentValueEntity.class);
        verify(persistentValueDao, times(1)).save(argCap.capture());
        assertEquals(value.getContent(), argCap.getValue().getContent());
        assertEquals("job-id", argCap.getValue().getJobId());
    }


    /**
     * Tests upserting an existing entity.
     */
    @Test
    @DisplayName("Tests upserting an existing entity.")
    void testUpsertExisting() {
        PersistentValue value = new PersistentValue();
        value.setId(123L);
        value.setContent("persistent-value-content");

        PersistentValueEntity entity = new PersistentValueEntity();
        entity.setId(456L);
        entity.setCreated(Instant.now());
        entity.setContent("must-be-replaced");

        when(persistentValueDao.findById(123L)).thenReturn(Optional.of(entity));

        when(persistentValueDao.save(entity)).thenReturn(entity);

        PersistentValue persistedValue = repository.upsert("job-id", value);

        assertEquals(persistedValue, value);

        ArgumentCaptor<PersistentValueEntity> argCap = ArgumentCaptor.forClass(PersistentValueEntity.class);
        verify(persistentValueDao, times(1)).save(argCap.capture());
        assertEquals(value.getContent(), argCap.getValue().getContent());
    }

    /**
     * Tests checking an entity for its persistence state.
     */
    @Test
    @DisplayName("Tests checking an entity for its persistence state.")
    void testIsPersisted() {
        assertFalse(repository.isPersisted(null, null));
        assertFalse(repository.isPersisted(null, new PersistentValue()));

        PersistentValue value = new PersistentValue();
        value.setContent("content");

        when(persistentValueDao.findByJobIdAndContent("job-id", "content")).thenReturn(new PersistentValueEntity());

        assertTrue(repository.isPersisted("job-id", value));
    }

    /**
     * Tests cleaning up old entities.
     */
    @Test
    @DisplayName("Tests cleaning up old entities.")
    void testCleanup() {
        repository.cleanup("job-id", 3);
        verify(persistentValueDao, times(0)).deleteByJobIdAndIdBefore(eq("job-id"), anyLong());

        when(persistentValueDao.findMostRecentIds("job-id", 3)).thenReturn(List.of(123, 456, 789));
        repository.cleanup("job-id", 3);
        verify(persistentValueDao, times(1)).deleteByJobIdAndIdBefore("job-id", 789L);
    }

    /**
     * Tests deleting entities.
     */
    @Test
    @DisplayName("Tests deleting entities.")
    void testDeleteByJobId() {
        repository.deleteByJobId(null);
        verify(persistentValueDao, times(0)).deleteByJobId(anyString());

        repository.deleteByJobId("job-id");
        verify(persistentValueDao, times(1)).deleteByJobId("job-id");
    }

}