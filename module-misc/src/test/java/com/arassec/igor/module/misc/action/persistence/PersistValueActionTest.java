package com.arassec.igor.module.misc.action.persistence;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.PersistentValue;
import com.arassec.igor.core.repository.PersistentValueRepository;
import com.arassec.igor.module.misc.action.BaseMiscActionTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link PersistValueAction}.
 */
@DisplayName("'Persist value' action tests.")
class PersistValueActionTest extends BaseMiscActionTest {

    /**
     * Tests processing the action with JSON-Path parameters.
     */
    @Test
    @DisplayName("Tests the action with JSON-Path parameters.")
    void testProcess() {
        PersistentValueRepository persistentValueRepositoryMock = mock(PersistentValueRepository.class);

        PersistValueAction action = new PersistValueAction(persistentValueRepositoryMock);
        action.setInput("$.data." + PARAM_KEY);

        ArgumentCaptor<PersistentValue> argCap = ArgumentCaptor.forClass(PersistentValue.class);

        List<Map<String, Object>> result = action.process(createData(), new JobExecution());

        assertEquals(1, result.size());
        verify(persistentValueRepositoryMock, times(1)).upsert(eq(JOB_ID), eq(TASK_ID), argCap.capture());
        assertEquals(PARAM_VALUE, argCap.getValue().getContent());
    }

    /**
     * Tests processing the action without a value to persist.
     */
    @Test
    @DisplayName("Tests the action without a value to persist.")
    void testProcessNoValue() {
        PersistentValueRepository persistentValueRepositoryMock = mock(PersistentValueRepository.class);

        PersistValueAction action = new PersistValueAction(persistentValueRepositoryMock);
        action.setInput("$.data.INVALID");

        List<Map<String, Object>> result = action.process(createData(), new JobExecution());
        assertTrue(result.isEmpty());
    }

    /**
     * Tests processing the action with already persisted data.
     */
    @Test
    @DisplayName("Tests the action with already persisted data.")
    void testProcessAlreadyPersisted() {
        PersistentValueRepository persistentValueRepositoryMock = mock(PersistentValueRepository.class);
        when(persistentValueRepositoryMock.isPersisted(eq(JOB_ID), eq(TASK_ID), any(PersistentValue.class))).thenReturn(true);

        PersistValueAction action = new PersistValueAction(persistentValueRepositoryMock);
        action.setInput("$.data." + PARAM_KEY);

        List<Map<String, Object>> result = action.process(createData(), new JobExecution());

        assertEquals(1, result.size());
        verify(persistentValueRepositoryMock, times(0)).upsert(eq(JOB_ID), eq(TASK_ID), any(PersistentValue.class));
    }

    /**
     * Tests the cleanup functionality of old persisted values after the job has run.
     */
    @Test
    @DisplayName("Tests the shutdown processing.")
    void testShutdown() {
        PersistentValueRepository persistentValueRepositoryMock = mock(PersistentValueRepository.class);

        PersistValueAction action = new PersistValueAction(persistentValueRepositoryMock);

        action.shutdown(JOB_ID, TASK_ID, new JobExecution());
        verify(persistentValueRepositoryMock, times(0)).cleanup(eq(JOB_ID), eq(TASK_ID), anyInt());

        action.setNumValuesToKeep(5);
        action.shutdown(JOB_ID, TASK_ID, new JobExecution());
        verify(persistentValueRepositoryMock, times(1)).cleanup(eq(JOB_ID), eq(TASK_ID), eq(5));
    }

}