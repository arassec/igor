package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.persistence.dao.JobExecutionDao;
import com.arassec.igor.persistence.entity.JobExecutionEntity;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link JdbcJobExecutionRepository}.
 */
@DisplayName("Job-Execution-Repository tests.")
class JdbcJobExecutionRepositoryTest {

    /**
     * The repository under test.
     */
    private JdbcJobExecutionRepository repository;

    /**
     * DAO for job-execution entities.
     */
    private JobExecutionDao jobExecutionDao;

    /**
     * ObjectMapper for JSON conversion.
     */
    private ObjectMapper persistenceJobMapper;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        jobExecutionDao = mock(JobExecutionDao.class);
        persistenceJobMapper = mock(ObjectMapper.class);
        repository = new JdbcJobExecutionRepository(jobExecutionDao, persistenceJobMapper);
    }

    /**
     * Tests saving a job-execution.
     */
    @Test
    @DisplayName("Tests saving a new job-execution.")
    void testUpsertNew() {
        JobExecution jobExecution = new JobExecution();
        jobExecution.setJobId("job-id");
        jobExecution.setExecutionState(JobExecutionState.RUNNING);

        JobExecutionEntity entity = new JobExecutionEntity();
        entity.setId(123L);
        when(jobExecutionDao.save(any(JobExecutionEntity.class))).thenReturn(entity);

        JobExecution upsertedJobExecution = repository.upsert(jobExecution);

        assertEquals(123L, upsertedJobExecution.getId());

        ArgumentCaptor<JobExecutionEntity> argCap = ArgumentCaptor.forClass(JobExecutionEntity.class);
        verify(jobExecutionDao, times(1)).save(argCap.capture());

        JobExecutionEntity value = argCap.getValue();
        assertEquals("job-id", value.getJobId());
        assertEquals(JobExecutionState.RUNNING.name(), value.getState());
    }

    /**
     * Tests updating an existing job-execution.
     */
    @Test
    @DisplayName("Tests updating an existing job-execution.")
    void testUpsertExisting() {
        JobExecution jobExecution = new JobExecution();
        jobExecution.setId(123L);
        jobExecution.setJobId("job-id");
        jobExecution.setExecutionState(JobExecutionState.RUNNING);

        JobExecutionEntity entity = new JobExecutionEntity();
        entity.setId(123L);
        when(jobExecutionDao.save(any(JobExecutionEntity.class))).thenReturn(entity);

        when(jobExecutionDao.findById(123L)).thenReturn(Optional.of(entity));

        repository.upsert(jobExecution);

        verify(jobExecutionDao, times(1)).save(entity);
    }

    /**
     * Tests error handling during job-execution upserts.
     */
    @Test
    @DisplayName("Tests error handling during job-execution upserts.")
    @SneakyThrows(JsonProcessingException.class)
    void testUpsertErrorHandling() {
        // null input -> null output
        assertNull(repository.upsert(null));

        // Error during JSON parsing:
        JobExecution jobExecution = new JobExecution();
        jobExecution.setJobId("job-id");
        jobExecution.setExecutionState(JobExecutionState.RUNNING);

        when(persistenceJobMapper.writeValueAsString(jobExecution)).thenThrow(mock(JsonProcessingException.class));

        assertThrows(IllegalStateException.class, () -> repository.upsert(jobExecution));

        // Job is deleted during upsert:
        when(jobExecutionDao.findById(anyLong())).thenReturn(Optional.empty());

        jobExecution.setId(123L);

        assertNull(repository.upsert(jobExecution));
    }

    /**
     * Tests finding a job-execution by its ID.
     */
    @Test
    @DisplayName("Tests finding a job-execution by its ID.")
    @SneakyThrows(JsonProcessingException.class)
    void testFindById() {
        assertNull(repository.findById(1L));

        JobExecutionEntity entity = new JobExecutionEntity();
        entity.setId(123L);
        entity.setState(JobExecutionState.WAITING.name());
        entity.setContent("{}");

        when(jobExecutionDao.findById(123L)).thenReturn(Optional.of(entity));

        JobExecution jobExecution = new JobExecution();
        when(persistenceJobMapper.readValue(anyString(), eq(JobExecution.class))).thenReturn(jobExecution);

        JobExecution foundJobExecution = repository.findById(123L);

        assertEquals(jobExecution, foundJobExecution);
    }

    /**
     * Tests finding all job-executions of a job.
     */
    @Test
    @DisplayName("Tests finding all job-executions of a job.")
    @SneakyThrows(JsonProcessingException.class)
    void testFindAllOfJob() {
        JobExecutionEntity jobExecutionEntity = new JobExecutionEntity();
        jobExecutionEntity.setId(666L);
        jobExecutionEntity.setState(JobExecutionState.FINISHED.name());
        jobExecutionEntity.setContent("execution-json");

        @SuppressWarnings("unchecked")
        Page<JobExecutionEntity> page = mock(Page.class);
        when(page.getNumber()).thenReturn(1);
        when(page.getSize()).thenReturn(2);
        when(page.getTotalPages()).thenReturn(3);
        when(page.getContent()).thenReturn(List.of(jobExecutionEntity));
        when(page.hasContent()).thenReturn(true);

        ArgumentCaptor<Pageable> argCap = ArgumentCaptor.forClass(Pageable.class);

        when(jobExecutionDao.findByJobId(eq("job-id"), argCap.capture())).thenReturn(page);

        when(persistenceJobMapper.readValue("execution-json", JobExecution.class)).thenReturn(new JobExecution());

        ModelPage<JobExecution> allOfJobPage = repository.findAllOfJob("job-id", 1, 2);

        assertEquals(1, allOfJobPage.getNumber());
        assertEquals(2, allOfJobPage.getSize());
        assertEquals(3, allOfJobPage.getTotalPages());
        assertEquals(1, allOfJobPage.getItems().size());

        assertEquals(666, allOfJobPage.getItems().get(0).getId());
        assertEquals(JobExecutionState.FINISHED, allOfJobPage.getItems().get(0).getExecutionState());

        assertEquals(1, argCap.getValue().getPageNumber());
        assertEquals(2, argCap.getValue().getPageSize());
        assertEquals(Sort.by("id").descending(), argCap.getValue().getSort());
    }

    /**
     * Tests counting job executions with a given state of a given job.
     */
    @Test
    @DisplayName("Tests counting job executions with a given state of a given job.")
    @SneakyThrows
    void testCountAllOfJobInState() {
        when(jobExecutionDao.countAllOfJobInState("job-id", JobExecutionState.FAILED.name())).thenReturn(23);
        assertEquals(23, repository.countAllOfJobInState("job-id", JobExecutionState.FAILED));
    }

    /**
     * Tests finding all job-executions of a job in a certain state.
     */
    @Test
    @DisplayName("Tests finding all job-executions of a job in a certain state.")
    @SneakyThrows(JsonProcessingException.class)
    void testFindAllOfJobInState() {
        assertTrue(repository.findAllOfJobInState("job-id", JobExecutionState.RUNNING).isEmpty());

        JobExecutionEntity jobExecutionEntity = new JobExecutionEntity();
        jobExecutionEntity.setId(666L);
        jobExecutionEntity.setState(JobExecutionState.FINISHED.name());
        jobExecutionEntity.setContent("execution-json");

        when(jobExecutionDao.findByJobIdAndStateOrderByIdDesc("job-id", "FINISHED")).thenReturn(List.of(jobExecutionEntity));

        JobExecution jobExecution = new JobExecution();
        when(persistenceJobMapper.readValue("execution-json", JobExecution.class)).thenReturn(jobExecution);

        List<JobExecution> allOfJobInState = repository.findAllOfJobInState("job-id", JobExecutionState.FINISHED);

        assertEquals(jobExecution, allOfJobInState.get(0));
        assertEquals(666, allOfJobInState.get(0).getId());
        assertEquals(JobExecutionState.FINISHED, allOfJobInState.get(0).getExecutionState());
    }

    /**
     * Tests finding all job-executions in a certain state.
     */
    @Test
    @DisplayName("Tests finding all job-executions in a certain state.")
    @SneakyThrows(JsonProcessingException.class)
    void testFindInState() {
        JobExecutionEntity jobExecutionEntity = new JobExecutionEntity();
        jobExecutionEntity.setId(666L);
        jobExecutionEntity.setState(JobExecutionState.RUNNING.name());
        jobExecutionEntity.setContent("execution-json");
        ArgumentCaptor<Pageable> argCap = ArgumentCaptor.forClass(Pageable.class);

        @SuppressWarnings("unchecked")
        Page<JobExecutionEntity> page = mock(Page.class);
        when(page.getNumber()).thenReturn(1);
        when(page.getSize()).thenReturn(2);
        when(page.getTotalPages()).thenReturn(3);
        when(page.getContent()).thenReturn(List.of(jobExecutionEntity));
        when(page.hasContent()).thenReturn(true);

        when(jobExecutionDao.findByState(eq(JobExecutionState.RUNNING.name()), argCap.capture())).thenReturn(page);

        when(persistenceJobMapper.readValue("execution-json", JobExecution.class)).thenReturn(new JobExecution());

        ModelPage<JobExecution> inStatePage = repository.findInState(JobExecutionState.RUNNING, 1, 2);

        assertEquals(1, inStatePage.getNumber());
        assertEquals(2, inStatePage.getSize());
        assertEquals(3, inStatePage.getTotalPages());
        assertEquals(1, inStatePage.getItems().size());

        assertEquals(666, inStatePage.getItems().get(0).getId());
        assertEquals(JobExecutionState.RUNNING, inStatePage.getItems().get(0).getExecutionState());

        assertEquals(1, argCap.getValue().getPageNumber());
        assertEquals(2, argCap.getValue().getPageSize());
        assertEquals(Sort.by("id").descending(), argCap.getValue().getSort());
    }

    /**
     * Tests cleaning up old job-executions of a job.
     */
    @Test
    @DisplayName("Tests cleaning up old job-executions of a job.")
    void testCleanup() {
        JobExecutionEntity first = new JobExecutionEntity();
        first.setId(4L);
        first.setState(JobExecutionState.WAITING.name());

        JobExecutionEntity second = new JobExecutionEntity();
        second.setId(3L);
        second.setState(JobExecutionState.FAILED.name());

        JobExecutionEntity third = new JobExecutionEntity();
        third.setId(2L);
        third.setState(JobExecutionState.FINISHED.name());

        @SuppressWarnings("unchecked")
        Page<JobExecutionEntity> page = mock(Page.class);
        when(page.hasContent()).thenReturn(true);
        when(page.getTotalElements()).thenReturn(3L);
        when(page.getContent()).thenReturn(List.of(first, second, third));

        when(jobExecutionDao.findByJobId(eq("job-id"), any(Pageable.class))).thenReturn(page);

        repository.cleanup("job-id", 1);

        verify(jobExecutionDao, times(1)).deleteByJobIdAndStateNotAndIdBefore("job-id",
                JobExecutionState.FAILED.name(), 4L);
    }

    /**
     * Tests deleting job-executions of a certain job.
     */
    @Test
    @DisplayName("Tests deleting job-executions of a certain job.")
    void testDeleteByJobId() {
        repository.deleteByJobId(null);
        verify(jobExecutionDao, times(0)).deleteByJobId(anyString());

        repository.deleteByJobId("job-id");
        verify(jobExecutionDao, times(1)).deleteByJobId(eq("job-id"));
    }

    /**
     * Tests updating all job-executions in a certain state.
     */
    @Test
    @DisplayName("Tests updating all job-executions in a certain state.")
    void testUpdateJobExecutionState() {
        repository.updateJobExecutionState(null, null);
        verify(jobExecutionDao, times(0)).findById(anyLong());

        repository.updateJobExecutionState(null, JobExecutionState.RESOLVED);
        verify(jobExecutionDao, times(0)).findById(anyLong());

        repository.updateJobExecutionState(123L, null);
        verify(jobExecutionDao, times(0)).findById(anyLong());

        JobExecutionEntity entity = new JobExecutionEntity();
        entity.setState(JobExecutionState.FAILED.name());

        when(jobExecutionDao.findById(123L)).thenReturn(Optional.of(entity));

        repository.updateJobExecutionState(123L, JobExecutionState.RESOLVED);

        assertEquals(JobExecutionState.RESOLVED.name(), entity.getState());
    }

    /**
     * Tests updating all job-executions of a certain job.
     */
    @Test
    @DisplayName("Tests updating all job-executions of a certain job.")
    void testUpdateAllJobExecutionsOfJob() {
        repository.updateAllJobExecutionsOfJob(null, null, null);
        verify(jobExecutionDao, times(0)).findByJobIdAndStateOrderByIdDesc(anyString(), anyString());
        repository.updateAllJobExecutionsOfJob("job-id", null, null);
        verify(jobExecutionDao, times(0)).findByJobIdAndStateOrderByIdDesc(anyString(), anyString());
        repository.updateAllJobExecutionsOfJob(null, JobExecutionState.FAILED, null);
        verify(jobExecutionDao, times(0)).findByJobIdAndStateOrderByIdDesc(anyString(), anyString());
        repository.updateAllJobExecutionsOfJob(null, null, JobExecutionState.RESOLVED);
        verify(jobExecutionDao, times(0)).findByJobIdAndStateOrderByIdDesc(anyString(), anyString());

        List<JobExecutionEntity> entities = List.of(new JobExecutionEntity(), new JobExecutionEntity());

        when(jobExecutionDao.findByJobIdAndStateOrderByIdDesc("job-id", JobExecutionState.FAILED.name())).thenReturn(entities);

        repository.updateAllJobExecutionsOfJob("job-id", JobExecutionState.FAILED, JobExecutionState.RESOLVED);

        entities.forEach(entity -> assertEquals(JobExecutionState.RESOLVED.name(), entity.getState()));
    }

    /**
     * Tests counting jobs with a given state.
     */
    @Test
    @DisplayName("Tests counting jobs with a given state.")
    void testCoutJobWithState() {
        when(jobExecutionDao.countDistinctJobIdByState(JobExecutionState.RUNNING.name())).thenReturn(42);
        assertEquals(42, repository.countJobsWithState(JobExecutionState.RUNNING));
    }

}