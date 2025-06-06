package com.arassec.igor.core.model.job.starter;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.concurrent.ConcurrencyGroup;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.trigger.EventTrigger;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.util.IgorException;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * {@link JobStarter} for event-triggered jobs.
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class EventTriggeredJobStarter extends DefaultJobStarter {

    /**
     * Stores the initial capacity of the queue that provides the trigger's input to the job.
     */
    private final int triggerEventInputQueueCapacity;

    /**
     * Creates a new instance.
     *
     * @param trigger      The job's trigger.
     * @param actions      The job's actions.
     * @param jobExecution The current job execution.
     * @param numThreads   The number of threads the job should execute with.
     */
    public EventTriggeredJobStarter(Trigger trigger, List<Action> actions, JobExecution jobExecution, int numThreads) {
        super(trigger, actions, jobExecution, numThreads);
        triggerEventInputQueueCapacity = numThreads;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConcurrencyGroup> process() {
        // Yet another input queue... This one is used by the trigger to put received data items in. Those are then received below
        // to put them in the input queue of the concurrency groups i.e., hand them over to the actions.
        BlockingQueue<Map<String, Object>> triggerEventInputQueue = new LinkedBlockingQueue<>(1);
        if (!concurrencyLists.isEmpty() && !concurrencyLists.getFirst().isEmpty()) {
            triggerEventInputQueue = new LinkedBlockingQueue<>(triggerEventInputQueueCapacity);
        }
        ((EventTrigger) trigger).setEventQueue(triggerEventInputQueue);

        // Initialize IgorComponents used by the job:
        initialize(jobExecution);

        // Block until igor is shut down and wait for trigger events:
        while (JobExecutionState.ACTIVE.equals(jobExecution.getExecutionState())) {
            Map<String, Object> eventData;
            try {
                // Receive the data item from the trigger...
                eventData = triggerEventInputQueue.poll(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IgorException("Interrupted during event polling!", e);
            }
            if (eventData != null) {
                log.debug("Job '{}' triggered by event: {}", jobExecution.getJobId(), eventData);
                eventData.putAll(trigger.createDataItem()); // A custom trigger might add additional data to the items.
                // ...and dispatch it to the waiting actions.
                dispatchInitialDataItem(initialInputQueue, eventData, processingFinishedCallbackSet);
                jobExecution.setProcessedEvents(jobExecution.getProcessedEvents() + 1);
            }
        }

        return concurrencyGroups;
    }

}
