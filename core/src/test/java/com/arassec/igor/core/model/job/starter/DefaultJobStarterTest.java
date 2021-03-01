package com.arassec.igor.core.model.job.starter;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.concurrent.ConcurrencyGroup;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ProcessingFinishedCallback;
import com.arassec.igor.core.model.trigger.Trigger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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

        assertThrows(IllegalArgumentException.class, () -> new DefaultJobStarter(null, null, null, 1));
        assertThrows(IllegalArgumentException.class, () -> new DefaultJobStarter(null, null, jobExecution, 1));
        assertThrows(IllegalArgumentException.class, () -> new DefaultJobStarter(null, actions, null, 1));

        assertDoesNotThrow(() -> new DefaultJobStarter(null, actions, jobExecution, 1));
    }

    /**
     * Tests creation of concurrency groups.
     */
    @Test
    @DisplayName("Tests creation of concurrency groups.")
    void testConcurrencyGroupCreation() {
        // First group:
        Action firstAction = mock(Action.class);
        when(firstAction.isActive()).thenReturn(true);

        // Ignored because inactive:
        Action secondAction = mock(Action.class);
        when(secondAction.enforceSingleThread()).thenReturn(true);

        Action thirdAction = mock(Action.class);
        when(thirdAction.isActive()).thenReturn(true);

        // Second group:
        Action fourthAction = mock(Action.class);
        when(fourthAction.enforceSingleThread()).thenReturn(true);
        when(fourthAction.isActive()).thenReturn(true);

        List<Action> actions = List.of(firstAction, secondAction, thirdAction, fourthAction);
        JobExecution jobExecution = JobExecution.builder().jobId("job-id").build();

        DefaultJobStarter defaultJobStarter = new DefaultJobStarter(null, actions, jobExecution, 3);

        assertEquals(2, defaultJobStarter.getConcurrencyGroups().size());
    }

    /**
     * Tests handling of a processing finished callback.
     */
    @Test
    @DisplayName("Tests handling of a processing finished callback.")
    void testProcessingFinishedCallbackHandling() {
        Action action = mock(Action.class);
        when(action.isActive()).thenReturn(true);

        Trigger trigger = mock(Trigger.class);

        DefaultJobStarter defaultJobStarter = new DefaultJobStarter(trigger, List.of(action), new JobExecution(), 1);

        assertFalse(defaultJobStarter.isProcessingFinishedCallbackSet());

        Trigger processingFinishedCallback = mock(Trigger.class,
                withSettings().extraInterfaces(ProcessingFinishedCallback.class));

        defaultJobStarter = new DefaultJobStarter(processingFinishedCallback, List.of(action), new JobExecution(), 1);

        assertTrue(defaultJobStarter.isProcessingFinishedCallbackSet());
    }

    /**
     * Tests processing the job.
     */
    @Test
    @DisplayName("Tests processing the job.")
    void testProcess() {
        Action action = mock(Action.class);
        when(action.isActive()).thenReturn(true);

        Trigger trigger = mock(Trigger.class);

        JobExecution jobExecution = JobExecution.builder().build();

        DefaultJobStarter defaultJobStarter = new DefaultJobStarter(trigger, List.of(action), jobExecution, 1);

        List<ConcurrencyGroup> concurrencyGroups = defaultJobStarter.process();

        assertNotNull(concurrencyGroups);
        verify(trigger, times(1)).initialize(jobExecution);
        verify(action, times(1)).initialize(jobExecution);
    }

}
