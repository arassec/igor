package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.core.model.trigger.Trigger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link Job} model.
 */
@DisplayName("Job Tests")
class JobTest {

    /**
     * Tests running an empty job.
     */
    @Test
    @DisplayName("Tests running an empty job.")
    void testRunEmptyJob() {
        Job job = new Job();
        JobExecution jobExecution = new JobExecution();

        job.run(jobExecution);

        assertEquals(JobExecutionState.FINISHED, jobExecution.getExecutionState());
        assertNotNull(jobExecution.getStarted());
        assertNotNull(jobExecution.getFinished());
    }

    /**
     * Tests running a minimal job with inactive task.
     */
    @Test
    @DisplayName("Tests running a minimal job with inactive task.")
    void testRunJobInactiveTask() {
        Job job = new Job();
        job.setId("job-id");
        JobExecution jobExecution = new JobExecution();

        Task taskMock = mock(Task.class);
        job.getTasks().add(taskMock);

        job.run(jobExecution);

        assertEquals(JobExecutionState.FINISHED, jobExecution.getExecutionState());

        // inactive Task:
        verify(taskMock, times(0)).initialize(eq("job-id"), eq(jobExecution));
        verify(taskMock, times(0)).run(eq("job-id"), eq(jobExecution));
        verify(taskMock, times(0)).shutdown(eq("job-id"), eq(jobExecution));
    }

    /**
     * Tests running a minimal job with active task.
     */
    @Test
    @DisplayName("Tests running a minimal job with active task.")
    void testRunJobActiveTask() {
        Job job = new Job();
        job.setId("job-id");
        JobExecution jobExecution = new JobExecution();

        Task taskMock = mock(Task.class);
        job.getTasks().add(taskMock);

        when(taskMock.isActive()).thenReturn(true);

        job.run(jobExecution);

        assertEquals(JobExecutionState.FINISHED, jobExecution.getExecutionState());
        verify(taskMock, times(1)).initialize(eq("job-id"), eq(jobExecution));
        verify(taskMock, times(1)).run(eq("job-id"), eq(jobExecution));
        verify(taskMock, times(1)).shutdown(eq("job-id"), eq(jobExecution));
    }

    /**
     * Tests running a job with a trigger.
     */
    @Test
    @DisplayName("Tests running a job with a trigger.")
    void testRunJobTrigger() {
        Job job = new Job();
        job.setId("job-id");
        JobExecution jobExecution = new JobExecution();

        Trigger triggerMock = mock(Trigger.class);
        job.setTrigger(triggerMock);

        job.run(jobExecution);

        verify(triggerMock, times(1)).initialize(eq("job-id"), isNull(), eq(jobExecution));
        verify(triggerMock, times(1)).shutdown(eq("job-id"), isNull(), eq(jobExecution));
    }

    /**
     * Tests cancelling a running job.
     */
    @Test
    @DisplayName("Tests cancelling a running job.")
    void testJobCancellation() {
        Job job = new Job();
        job.setId("job-id");
        JobExecution jobExecution = new JobExecution();

        Task firstTaskMock = mock(Task.class);
        when(firstTaskMock.isActive()).thenReturn(true);
        Task secondTaskMock = mock(Task.class);
        when(secondTaskMock.isActive()).thenReturn(true);

        job.getTasks().add(firstTaskMock);
        job.getTasks().add(secondTaskMock);

        doAnswer(invocation -> {
            jobExecution.setExecutionState(JobExecutionState.CANCELLED);
            return null;
        }).when(firstTaskMock).run(eq("job-id"), eq(jobExecution));

        job.run(jobExecution);

        verify(firstTaskMock, times(1)).run(eq("job-id"), eq(jobExecution));
        verify(secondTaskMock, times(0)).run(eq("job-id"), eq(jobExecution));
    }

    /**
     * Tests failing safe on job execution errors.
     */
    @Test
    @DisplayName("Tests failing safe on execution errors.")
    void testFailSafe() {
        Job job = new Job();
        job.setId("job-id");
        JobExecution jobExecution = new JobExecution();

        Task taskMock = mock(Task.class);
        when(taskMock.isActive()).thenReturn(true);

        job.getTasks().add(taskMock);

        doThrow(new ServiceException("wanted-test-exception")).when(taskMock).run(eq("job-id"), eq(jobExecution));

        job.run(jobExecution);

        assertEquals(JobExecutionState.FAILED, jobExecution.getExecutionState());
    }

    /**
     * Tests, that a job creates its own execution instance if none is provided.
     */
    @Test
    @DisplayName("Tests creating a job-execution instance.")
    void testCreateJobExecutionInstance() {
        Job job = new Job();

        job.run(null);

        assertEquals(JobExecutionState.FINISHED, job.getCurrentJobExecution().getExecutionState());
        assertNotNull(job.getCurrentJobExecution().getStarted());
        assertNotNull(job.getCurrentJobExecution().getFinished());
    }

}
