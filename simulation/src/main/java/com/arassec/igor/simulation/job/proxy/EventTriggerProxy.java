package com.arassec.igor.simulation.job.proxy;

import com.arassec.igor.core.model.trigger.EventTrigger;
import com.arassec.igor.core.model.trigger.EventType;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Proxy for {@link EventTrigger} implementations. Used during simulated job executions to record the received incoming data
 * items.
 */
public class EventTriggerProxy extends TriggerProxy implements EventTrigger {

    /**
     * Contains the data items received by the trigger during a simulated job execution.
     */
    @Getter
    private final List<Map<String, Object>> triggerDataItems = new LinkedList<>();

    /**
     * Creates a new instance.
     *
     * @param delegate        The real trigger to use for method invocations.
     * @param simulationLimit The maximum number of data items that should be processed in a simulated job execution.
     */
    public EventTriggerProxy(EventTrigger delegate, int simulationLimit) {
        super(delegate, simulationLimit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEventQueue(BlockingQueue<Map<String, Object>> queue) {
        ((EventTrigger) delegate).setEventQueue(queue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processEvent(Map<String, Object> eventData) {
        processed++;
        if (processed > simulationLimit) {
            return;
        }
        Map<String, Object> data = delegate.getData();
        eventData.forEach(data::put);
        triggerDataItems.add(data);
        ((EventTrigger) delegate).processEvent(eventData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processingFinished(Map<String, Object> dataItem) {
        ((EventTrigger) delegate).processingFinished(dataItem);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> getSimulationTriggerData() {
        return triggerDataItems;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventType getSupportedEventType() {
        return ((EventTrigger) delegate).getSupportedEventType();
    }

}
