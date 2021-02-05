package com.arassec.igor.core.model.job.starter;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.concurrent.ConcurrencyGroup;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ProcessingFinishedCallback;
import com.arassec.igor.core.model.trigger.Trigger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link DefaultJobStarter}.
 */
@DisplayName("Default Job-Starter tests.")
class DefaultJobStarterTest {

    /**
     * Tests input validation during instance creation.
     */
    @Test
    @DisplayName("Tests input validation during instance creation.")
    void testInstanceCreationInputValidation() {
        List<Action> actions = List.of();
        JobExecution jobExecution = new JobExecution();

        assertThrows(IllegalArgumentException.class, () -> new DefaultJobStarter(null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new DefaultJobStarter(null, null, jobExecution));
        assertThrows(IllegalArgumentException.class, () -> new DefaultJobStarter(null, actions, null));

        assertDoesNotThrow(() -> new DefaultJobStarter(null, actions, jobExecution));
    }

    /**
     * Tests creation of concurrency groups.
     */
    @Test
    @DisplayName("Tests creation of concurrency groups.")
    void testConcurrencyGroupCreation() {
        // First group:
        Action firstAction = mock(Action.class);
        when(firstAction.getNumThreads()).thenReturn(2);
        when(firstAction.isActive()).thenReturn(true);

        // Ignored because inactive:
        Action secondAction = mock(Action.class);
        when(secondAction.getNumThreads()).thenReturn(1);

        // Second group:
        Action thirdAction = mock(Action.class);
        when(thirdAction.getNumThreads()).thenReturn(3);
        when(thirdAction.isActive()).thenReturn(true);
        Action fourthAction = mock(Action.class);
        when(fourthAction.getNumThreads()).thenReturn(3);
        when(fourthAction.isActive()).thenReturn(true);

        List<Action> actions = List.of(firstAction, secondAction, thirdAction, fourthAction);
        JobExecution jobExecution = JobExecution.builder().jobId("job-id").build();

        DefaultJobStarter defaultJobStarter = new DefaultJobStarter(null, actions, jobExecution);

        assertEquals(2, defaultJobStarter.getConcurrencyGroups().size());
    }

    /**
     * Tests handling of a processing finished callback.
     */
    @Test
    @DisplayName("Tests handling of a processing finished callback.")
    void testProcessingFinishedCallbackHandling() {
        Action action = mock(Action.class);
        when(action.getNumThreads()).thenReturn(1);
        when(action.isActive()).thenReturn(true);

        Trigger trigger = mock(Trigger.class);

        DefaultJobStarter defaultJobStarter = new DefaultJobStarter(trigger, List.of(action), new JobExecution());

        assertFalse(defaultJobStarter.isProcessingFinishedCallbackSet());

        Trigger processingFinishedCallback = mock(Trigger.class,
                withSettings().extraInterfaces(ProcessingFinishedCallback.class));

        defaultJobStarter = new DefaultJobStarter(processingFinishedCallback, List.of(action), new JobExecution());

        assertTrue(defaultJobStarter.isProcessingFinishedCallbackSet());
    }

    /**
     * Tests processing the job.
     */
    @Test
    @DisplayName("Tests processing the job.")
    void testProcess() {
        Action action = mock(Action.class);
        when(action.getNumThreads()).thenReturn(1);
        when(action.isActive()).thenReturn(true);

        Trigger trigger = mock(Trigger.class);

        JobExecution jobExecution = JobExecution.builder().build();

        DefaultJobStarter defaultJobStarter = new DefaultJobStarter(trigger, List.of(action), jobExecution);

        List<ConcurrencyGroup> concurrencyGroups = defaultJobStarter.process();

        assertNotNull(concurrencyGroups);
        verify(trigger, times(1)).initialize(jobExecution);
        verify(action, times(1)).initialize(jobExecution);
    }

    /**
     * Tests creating the meta-data for a job's data item.
     */
    @Test
    @DisplayName("Tests creating job meta-data.")
    void testCreateMetaData() {
        Trigger triggerMock = mock(Trigger.class);
        when(triggerMock.getMetaData()).thenReturn(Map.of(DataKey.SIMULATION.getKey(), true));
        Map<String, Object> metaData = DefaultJobStarter.createMetaData("job-id", null);
        assertEquals("job-id", metaData.get(DataKey.JOB_ID.getKey()));
        assertNotNull(metaData.get(DataKey.TIMESTAMP.getKey()));
        assertNull(metaData.get(DataKey.SIMULATION.getKey()));
    }

}
