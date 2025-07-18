package com.arassec.igor.simulation.job.proxy;

import com.arassec.igor.core.model.trigger.EventTrigger;
import com.arassec.igor.core.model.trigger.EventType;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Proxy for {@link EventTrigger} implementations. Used during simulated job executions to record the received incoming data
 * items.
 */
public class EventTriggerProxy extends TriggerProxy implements EventTrigger {

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

        // Add the message data to the collected data item here. During real job execution, this is done by the job.
        // This is only to display the initial data items to the user, during the simulated job execution, the original
        // ones created by the job will be used.
        Map<String, Object> dataItem = getDataItem();
        dataItem.putAll(eventData);
        getCollectedData().add(dataItem);

        ((EventTrigger) delegate).processEvent(eventData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> createDataItem() {
        return getDataItem();
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
    public EventType getSupportedEventType() {
        return ((EventTrigger) delegate).getSupportedEventType();
    }

}
