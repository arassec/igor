package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.trigger.EventTrigger;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.util.IgorException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
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

        job.start(jobExecution);

        assertEquals(JobExecutionState.FINISHED, jobExecution.getExecutionState());
        assertNotNull(jobExecution.getStarted());
        assertNotNull(jobExecution.getFinished());
    }

    /**
     * Tests running a minimal, inactive job.
     */
    @Test
    @DisplayName("Tests running a minimal, inactive job.")
    void testRunJobInactive() {
        Job job = Job.builder().id("job-id").active(false).build();
        JobExecution jobExecution = new JobExecution();

        job.start(jobExecution);

        assertEquals(JobExecutionState.FINISHED, jobExecution.getExecutionState());
    }

    /**
     * Tests running a minimal, active job.
     */
    @Test
    @DisplayName("Tests running a minimal, active job.")
    void testRunJobActive() {
        Job job = Job.builder().id("job-id").active(true).build();
        JobExecution jobExecution = new JobExecution();

        job.start(jobExecution);

        assertEquals(JobExecutionState.FINISHED, jobExecution.getExecutionState());
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

        job.start(jobExecution);

        verify(triggerMock, times(1)).initialize(jobExecution);
        verify(triggerMock, times(1)).shutdown(jobExecution);
    }

    /**
     * Tests execution state after cancelling a running job by calling the cancel() method.
     */
    @Test
    @DisplayName("Tests execution state after cancelling a running job by calling the cancel() method.")
    void testJobCancellationExecutionState() {
        Job job = new Job();
        job.setId("job-id");

        job.cancel(); // nothing happens without a job execution...

        JobExecution jobExecution = new JobExecution();
        jobExecution.setExecutionState(JobExecutionState.RUNNING);

        job.setCurrentJobExecution(jobExecution);

        job.cancel(); // RUNNING -> CANCELLED

        assertEquals(JobExecutionState.CANCELLED, jobExecution.getExecutionState());

        jobExecution.setExecutionState(JobExecutionState.ACTIVE);

        job.cancel(); // ACTIVE -> FINISHED

        assertEquals(JobExecutionState.FINISHED, jobExecution.getExecutionState());
    }

    /**
     * Tests calling the {@link Job#cancel()} method and waiting for actions to finish.
     */
    @Test
    @DisplayName("Tests calling the {@link Job#cancel()} method and waiting for actions to finish")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SneakyThrows
    void testJobCancellationByMethod() {
        JobExecution jobExecution = new JobExecution();
        jobExecution.setExecutionState(JobExecutionState.ACTIVE);

        Trigger eventTriggerMock = mock(EventTrigger.class);

        Action actionMock = mock(Action.class);
        when(actionMock.isActive()).thenReturn(true);
        when(actionMock.getNumThreads()).thenReturn(1);

        Job job = Job.builder()
                .id("job-id")
                .trigger(eventTriggerMock)
                .actions(List.of(actionMock)).build();

        ExecutorService testExecutor = Executors.newSingleThreadExecutor();
        testExecutor.execute(() -> job.start(jobExecution));

        // Wait 100 ms until the job really started and waits for incoming events:
        CountDownLatch waiter = new CountDownLatch(1);
        waiter.await(100, TimeUnit.MILLISECONDS);

        job.cancel();

        assertEquals(JobExecutionState.FINISHED, jobExecution.getExecutionState());

        verify(actionMock, times(1)).complete();
        verify(actionMock, times(1)).shutdown(jobExecution);

        testExecutor.shutdown();
    }

    /**
     * Tests failing safe on job execution errors.
     */
    @Test
    @DisplayName("Tests failing safe on execution errors.")
    void testFailSafe() {
        Trigger triggerMock = mock(Trigger.class);
        doThrow(new IgorException("Test-Exception!")).when(triggerMock).initialize(any(JobExecution.class));

        Job job = Job.builder().id("job-id").trigger(triggerMock).build();
        JobExecution jobExecution = new JobExecution();

        job.start(jobExecution);

        assertEquals(JobExecutionState.FAILED, jobExecution.getExecutionState());
    }

    /**
     * Tests, that a job creates its own execution instance if none is provided.
     */
    @Test
    @DisplayName("Tests creating a job-execution instance.")
    void testCreateJobExecutionInstance() {
        Job job = new Job();

        job.start(null);

        assertEquals(JobExecutionState.FINISHED, job.getCurrentJobExecution().getExecutionState());
        assertNotNull(job.getCurrentJobExecution().getStarted());
        assertNotNull(job.getCurrentJobExecution().getFinished());
    }

    /**
     * Tests default properties.
     */
    @Test
    @DisplayName("Tests default properties.")
    void testDefaultProperties() {
        Job job = new Job();
        assertEquals(5, job.getHistoryLimit());
        assertFalse(job.isRunning());
        assertTrue(job.isFaultTolerant());
    }

    /**
     * Tests running a minimal job with a trigger and an action.
     */
    @Test
    @DisplayName("Tests running a minimal job.")
    void testRunMinimalJob() {
        Trigger triggerMock = mock(Trigger.class);
        when(triggerMock.createDataItem()).thenReturn(Map.of("test", "input"));

        Action actionMock = mock(Action.class);
        when(actionMock.isActive()).thenReturn(true);
        when(actionMock.getNumThreads()).thenReturn(1);

        Job job = Job.builder().id("job-id").trigger(triggerMock).actions(List.of(actionMock)).build();

        JobExecution jobExecution = new JobExecution();
        jobExecution.setExecutionState(JobExecutionState.RUNNING);

        job.start(jobExecution);

        verify(triggerMock, times(1)).initialize(jobExecution);
        verify(actionMock, times(1)).initialize(jobExecution);
        verify(actionMock, times(1)).process(anyMap(), eq(jobExecution));
        verify(actionMock, times(1)).shutdown(jobExecution);
        verify(triggerMock, times(1)).shutdown(jobExecution);
    }

}
