package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.provider.Provider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link Task} model.
 */
@DisplayName("Task Tests")
class TaskTest {

    /**
     * Tests running an empty task.
     */
    @Test
    @DisplayName("Tests running an empty task.")
    void testRunEmptyTask() {
        Task task = new Task();
        task.setName("test-task");
        JobExecution jobExecution = new JobExecution();

        task.run("job-id", jobExecution);

        assertEquals("test-task", jobExecution.getCurrentTask());
        assertFalse(task.isInitialized());
    }

    /**
     * Tests running a minimal task with a provider.
     */
    @Test
    @DisplayName("Tests running a minimal task.")
    void testRunMinimalTask() {
        Task task = new Task();
        task.setName("test-task");
        task.setInitialized(true);

        JobExecution jobExecution = new JobExecution();
        jobExecution.setExecutionState(JobExecutionState.RUNNING);

        Map<String, Object> dataItem = new HashMap<>();

        Provider providerMock = mock(Provider.class);
        when(providerMock.hasNext()).thenReturn(true).thenReturn(false);
        when(providerMock.next()).thenReturn(dataItem);
        task.setProvider(providerMock);

        Action actionMock = mock(Action.class);
        when(actionMock.isActive()).thenReturn(true);
        when(actionMock.getNumThreads()).thenReturn(1);
        task.getActions().add(actionMock);

        task.run("job-id", jobExecution);

        verify(providerMock, times(2)).hasNext();
        verify(providerMock, times(1)).next();
        verify(actionMock, times(1)).process(anyMap(), eq(jobExecution));
    }

}
