package com.arassec.igor.module.misc.action.persistence;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.PersistentValue;
import com.arassec.igor.core.repository.PersistentValueRepository;
import com.arassec.igor.module.misc.action.MiscActionBaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link FilterPersistedValueAction}.
 */
@DisplayName("'Filter persisted value' action tests.")
class FilterPersistedValueActionTest extends MiscActionBaseTest {

    /**
     * Tests the action failing safe if no value can be retrieved.
     */
    @Test
    @DisplayName("Tests the action without a value.")
    void testProcessNoValue() {
        PersistentValueRepository persistentValueRepositoryMock = mock(PersistentValueRepository.class);

        FilterPersistedValueAction action = new FilterPersistedValueAction(persistentValueRepositoryMock);
        action.setInput("$.data.INVALID");

        List<Map<String, Object>> result = action.process(createData(), new JobExecution());
        assertTrue(result.isEmpty());
        verify(persistentValueRepositoryMock, times(0)).isPersisted(eq(JOB_ID), any(PersistentValue.class));
    }

    /**
     * Tests the action with an already persisted value.
     */
    @Test
    @DisplayName("Tests the action with an already persisted value.")
    void testProcessAlreadyPersisted() {
        PersistentValueRepository persistentValueRepositoryMock = mock(PersistentValueRepository.class);
        when(persistentValueRepositoryMock.isPersisted(eq(JOB_ID), any(PersistentValue.class))).thenReturn(true);

        FilterPersistedValueAction action = new FilterPersistedValueAction(persistentValueRepositoryMock);
        action.setInput("$.data." + PARAM_KEY);

        List<Map<String, Object>> result = action.process(createData(), new JobExecution());
        assertTrue(result.isEmpty());
    }

    /**
     * Tests the action with a new value.
     */
    @Test
    @DisplayName("Tests the action with a non-persistent value.")
    void testProcessNonPersistent() {
        PersistentValueRepository persistentValueRepositoryMock = mock(PersistentValueRepository.class);

        FilterPersistedValueAction action = new FilterPersistedValueAction(persistentValueRepositoryMock);
        action.setInput("$.data." + PARAM_KEY);

        List<Map<String, Object>> result = action.process(createData(), new JobExecution());
        assertFalse(result.isEmpty());

        ArgumentCaptor<PersistentValue> argCap = ArgumentCaptor.forClass(PersistentValue.class);
        verify(persistentValueRepositoryMock, times(1)).isPersisted(eq(JOB_ID), argCap.capture());
        assertEquals(PARAM_VALUE, argCap.getValue().getContent());
    }

}