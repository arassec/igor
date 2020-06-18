package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.util.IgorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

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

        job.run(jobExecution);

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

        job.run(jobExecution);

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

        job.run(jobExecution);

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

        job.run(jobExecution);

        verify(triggerMock, times(1)).initialize(eq("job-id"), eq(jobExecution));
        verify(triggerMock, times(1)).shutdown(eq("job-id"), eq(jobExecution));
    }

    /**
     * Tests calling the {@link Job#cancel()} method.
     */
    @Test
    @DisplayName("Tests cancelling a running job by calling the cancel() method.")
    void testJobCancellationByMethod() {
        Job job = new Job();
        job.setId("job-id");

        JobExecution jobExecution = new JobExecution();
        jobExecution.setExecutionState(JobExecutionState.RUNNING);

        job.setCurrentJobExecution(jobExecution);

        job.cancel();

        assertEquals(JobExecutionState.CANCELLED, jobExecution.getExecutionState());
    }

    /**
     * Tests failing safe on job execution errors.
     */
    @Test
    @DisplayName("Tests failing safe on execution errors.")
    void testFailSafe() {
        Job job = Job.builder().id("job-id").build();
        JobExecution jobExecution = new JobExecution();

        Provider providerMock = mock(Provider.class);
        doThrow(new IgorException("wanted-test-exception")).when(providerMock).hasNext();

        job.setProvider(providerMock);

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
     * Tests running a minimal job with a provider.
     */
    @Test
    @DisplayName("Tests running a minimal job.")
    void testRunMinimalJob() {
        Job job = Job.builder().id("job-id").build();

        JobExecution jobExecution = new JobExecution();
        jobExecution.setExecutionState(JobExecutionState.RUNNING);

        Map<String, Object> dataItem = new HashMap<>();

        Provider providerMock = mock(Provider.class);
        when(providerMock.hasNext()).thenReturn(true).thenReturn(false);
        when(providerMock.next()).thenReturn(dataItem);
        job.setProvider(providerMock);

        Action actionMock = mock(Action.class);
        when(actionMock.isActive()).thenReturn(true);
        when(actionMock.getNumThreads()).thenReturn(1);
        job.getActions().add(actionMock);

        job.run(jobExecution);

        verify(providerMock, times(1)).initialize(eq("job-id"), eq(jobExecution));
        verify(actionMock, times(1)).initialize(eq("job-id"), eq(jobExecution));

        verify(providerMock, times(2)).hasNext();
        verify(providerMock, times(1)).next();
        verify(actionMock, times(1)).process(anyMap(), eq(jobExecution));

        verify(providerMock, times(1)).shutdown(eq("job-id"), eq(jobExecution));
        verify(actionMock, times(1)).shutdown(eq("job-id"), eq(jobExecution));
    }

    /**
     * Tests creating the meta-data for a job's data item.
     */
    @Test
    @DisplayName("Tests creating job meta-data.")
    void testCreateMetaData() {
        Map<String, Object> metaData = Job.createMetaData("job-id");
        assertEquals("job-id", metaData.get(DataKey.JOB_ID.getKey()));
        assertNotNull(metaData.get(DataKey.TIMESTAMP.getKey()));
    }

}
