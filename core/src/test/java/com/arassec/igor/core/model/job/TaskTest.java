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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    }

    /**
     * Tests running a minimal task with a provider.
     */
    @Test
    @DisplayName("Tests running a minimal task.")
    void testRunMinimalTask() {
        Task task = new Task();
        task.setName("test-task");
        task.setId("task-id");

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

        verify(providerMock, times(1)).initialize(eq("job-id"), eq("task-id"), eq(jobExecution));
        verify(actionMock, times(1)).initialize(eq("job-id"), eq("task-id"), eq(jobExecution));

        verify(providerMock, times(2)).hasNext();
        verify(providerMock, times(1)).next();
        verify(actionMock, times(1)).process(anyMap(), eq(jobExecution));

        verify(providerMock, times(1)).shutdown(eq("job-id"), eq("task-id"), eq(jobExecution));
        verify(actionMock, times(1)).shutdown(eq("job-id"), eq("task-id"), eq(jobExecution));
    }

    /**
     * Tests creating the meta-data for a task data item.
     */
    @Test
    @DisplayName("Tests creating job meta-data.")
    void testCreateMetaData() {
        Map<String, Object> metaData = Task.createMetaData("job-id", "task-id");
        assertEquals("job-id", metaData.get(Task.JOB_ID_KEY));
        assertEquals("task-id", metaData.get(Task.TASK_ID_KEY));
        assertNotNull(metaData.get(Task.TIMESTAMP_KEY));
    }

}
