package com.arassec.igor.simulation.job;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.trigger.EventTrigger;
import com.arassec.igor.core.model.trigger.EventType;
import com.arassec.igor.core.util.event.JobTriggerEvent;
import com.arassec.igor.simulation.IgorSimulationProperties;
import com.arassec.igor.simulation.job.strategy.SimulationStrategy;
import com.arassec.igor.simulation.job.strategy.SimulationStrategyFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link StandardJobSimulator}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Standard Job-Simulator tests.")
class StandardJobSimulatorTest {

    /**
     * The class under test.
     */
    @InjectMocks
    private StandardJobSimulator standardJobSimulator;

    /**
     * Simulation configuration properties.
     */
    @Mock
    private IgorSimulationProperties simulationProperties;

    /**
     * A factory for {@link SimulationStrategy} instances.
     */
    @Mock
    private SimulationStrategyFactory simulationStrategyFactory;

    /**
     * Tests simulating a job.
     */
    @Test
    @DisplayName("Tests simulating a job.")
    void testSimulateJob() {
        Job job = Job.builder().build();

        SimulationStrategy simulationStrategyMock = mock(SimulationStrategy.class);

        when(simulationStrategyFactory.determineSimulationStrategy(job)).thenReturn(simulationStrategyMock);

        standardJobSimulator.simulateJob(job);

        verify(simulationStrategyMock, times(1)).simulate(eq(job), any(JobExecution.class));

        assertTrue(standardJobSimulator.getRunningSimulations().isEmpty());
    }

    /**
     * Tests cancelling all simulated job executions.
     */
    @Test
    @DisplayName("Tests cancelling all simulated job executions.")
    void testCancelAllSimulations() {
        Job jobMock = mock(Job.class);
        when(jobMock.getId()).thenReturn("job-id");

        standardJobSimulator.getRunningSimulations().add(jobMock);

        standardJobSimulator.cancelAllSimulations("job-id");

        verify(jobMock, times(1)).cancel();
    }

    /**
     * Tests reaction of the job simulator on job trigger events.
     */
    @Test
    @DisplayName("Tests reaction of the job simulator on job trigger events.")
    void testOnJobTriggerEvent() {
        EventTrigger eventTriggerMock = mock(EventTrigger.class);
        when(eventTriggerMock.getSupportedEventType()).thenReturn(EventType.MESSAGE);

        Job job = Job.builder().id("job-id").trigger(eventTriggerMock).build();

        standardJobSimulator.getRunningSimulations().add(job);

        // This event must be ignored due to the job ID.
        JobTriggerEvent invalidJobIdEvent = new JobTriggerEvent("another-job-id", Map.of(), EventType.MESSAGE);
        standardJobSimulator.onJobTriggerEvent(invalidJobIdEvent);
        verify(eventTriggerMock, times(0)).processEvent(anyMap());

        // This event must be ignored due to the event type.
        JobTriggerEvent invalidTypeEvent = new JobTriggerEvent("job-id", Map.of(), EventType.WEB_HOOK);
        standardJobSimulator.onJobTriggerEvent(invalidTypeEvent);
        verify(eventTriggerMock, times(0)).processEvent(anyMap());

        // This event must be processed:
        Map<String, Object> eventData = new HashMap<>();
        JobTriggerEvent validEvent = new JobTriggerEvent("job-id", eventData, EventType.MESSAGE);
        standardJobSimulator.onJobTriggerEvent(validEvent);
        verify(eventTriggerMock, times(1)).processEvent(eventData);
    }

    /**
     * Tests cancelling stale simulations.
     */
    @Test
    @DisplayName("Tests cancelling stale simulations.")
    void testCancelStaleSimulations() {
        when(simulationProperties.getTimeout()).thenReturn(0L); // Timeout of zero -> cancel job immediately.

        // Stale due to missing job execution:
        Job missingJobExecutionJob = mock(Job.class);

        // Stale due to timeout:
        JobExecution staleJobExecution = new JobExecution();
        staleJobExecution.setStarted(Instant.now());
        Job staleJob = mock(Job.class);
        when(staleJob.getCurrentJobExecution()).thenReturn(staleJobExecution);

        // Not stale, starts 15 minutes in the future:
        JobExecution futureJobExecution = new JobExecution();
        futureJobExecution.setStarted(Instant.ofEpochSecond(Instant.now().getEpochSecond() + 900));
        Job futureJob = mock(Job.class);
        when(futureJob.getCurrentJobExecution()).thenReturn(futureJobExecution);

        standardJobSimulator.getRunningSimulations().add(missingJobExecutionJob);
        standardJobSimulator.getRunningSimulations().add(staleJob);
        standardJobSimulator.getRunningSimulations().add(futureJob);

        standardJobSimulator.cancelStaleSimulations();

        verify(missingJobExecutionJob, times(1)).cancel();
        verify(staleJob, times(1)).cancel();
        verify(futureJob, times(0)).cancel();
    }

}
