package com.arassec.igor.plugin.core.persistence.action;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.PersistentValue;
import com.arassec.igor.core.repository.PersistentValueRepository;
import com.arassec.igor.plugin.core.CoreActionBaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link PersistValueAction}.
 */
@DisplayName("'Persist value' action tests.")
class PersistValueActionTest extends CoreActionBaseTest {

    /**
     * Tests the action with mustache template parameters.
     */
    @Test
    @DisplayName("Tests the action with mustache template parameters.")
    void testProcess() {
        PersistentValueRepository persistentValueRepositoryMock = mock(PersistentValueRepository.class);

        PersistValueAction action = new PersistValueAction(persistentValueRepositoryMock);
        action.setInput("{{" + DataKey.DATA.getKey() + "." + PARAM_KEY + "}}");

        ArgumentCaptor<PersistentValue> argCap = ArgumentCaptor.forClass(PersistentValue.class);

        List<Map<String, Object>> result = action.process(createData(), new JobExecution());

        assertEquals(1, result.size());
        verify(persistentValueRepositoryMock, times(1)).upsert(eq(JOB_ID), argCap.capture());
        assertEquals(PARAM_VALUE, argCap.getValue().getContent());
    }

    /**
     * Tests the action with an invalid template.
     */
    @Test
    @DisplayName("Tests the action with an invalid template.")
    void testProcessNoValue() {
        PersistentValueRepository persistentValueRepositoryMock = mock(PersistentValueRepository.class);

        PersistValueAction action = new PersistValueAction(persistentValueRepositoryMock);
        action.setInput("{{" + DataKey.DATA.getKey() + ".INVALID}}");

        action.process(createData(), new JobExecution());

        ArgumentCaptor<PersistentValue> argCap = ArgumentCaptor.forClass(PersistentValue.class);
        verify(persistentValueRepositoryMock, times(1)).upsert(eq("job-id"), argCap.capture());
        assertEquals("{{" + DataKey.DATA.getKey() + ".INVALID}}", argCap.getValue().getContent());
    }

    /**
     * Tests processing the action with already persisted data.
     */
    @Test
    @DisplayName("Tests the action with already persisted data.")
    void testProcessAlreadyPersisted() {
        PersistentValueRepository persistentValueRepositoryMock = mock(PersistentValueRepository.class);
        when(persistentValueRepositoryMock.isPersisted(eq(JOB_ID), any(PersistentValue.class))).thenReturn(true);

        PersistValueAction action = new PersistValueAction(persistentValueRepositoryMock);
        action.setInput("{{" + DataKey.DATA.getKey() + "." + PARAM_KEY + "}}");

        List<Map<String, Object>> result = action.process(createData(), new JobExecution());

        assertEquals(1, result.size());
        verify(persistentValueRepositoryMock, times(0)).upsert(eq(JOB_ID), any(PersistentValue.class));
    }

    /**
     * Tests the cleanup functionality of old persisted values after the job has run.
     */
    @Test
    @DisplayName("Tests the shutdown processing.")
    void testShutdown() {
        PersistentValueRepository persistentValueRepositoryMock = mock(PersistentValueRepository.class);

        PersistValueAction action = new PersistValueAction(persistentValueRepositoryMock);

        JobExecution jobExecution = JobExecution.builder().jobId(JOB_ID).build();

        action.shutdown(jobExecution);
        verify(persistentValueRepositoryMock, times(0)).cleanup(eq(JOB_ID), anyInt());

        action.setNumValuesToKeep(5);
        action.shutdown(jobExecution);
        verify(persistentValueRepositoryMock, times(1)).cleanup(JOB_ID, 5);
    }

    /**
     * Tests processing the action in simulation mode.
     */
    @Test
    @DisplayName("Tests processing the action in simulation mode.")
    void testProcessInSimulationMode() {
        PersistentValueRepository persistentValueRepositoryMock = mock(PersistentValueRepository.class);

        PersistValueAction action = new PersistValueAction(persistentValueRepositoryMock);
        action.setInput("{{" + DataKey.DATA.getKey() + "." + PARAM_KEY + "}}");

        Map<String, Object> data = createData();

        //noinspection unchecked
        ((Map<String, Object>) data.get(DataKey.META.getKey())).put(DataKey.SIMULATION.getKey(), true);

        List<Map<String, Object>> result = action.process(data, new JobExecution());

        assertEquals(1, result.size());
        verify(persistentValueRepositoryMock, times(0)).upsert(eq(JOB_ID), any(PersistentValue.class));
        assertEquals("Would have persisted: igor-message-test", result.getFirst().get(DataKey.SIMULATION_LOG.getKey()));
    }

}
