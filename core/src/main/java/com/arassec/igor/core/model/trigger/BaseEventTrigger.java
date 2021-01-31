package com.arassec.igor.core.model.trigger;

import com.arassec.igor.core.util.IgorException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

/**
 * Base for event based triggers. Provides sensible default implementations for most event based triggers.
 */
public abstract class BaseEventTrigger extends BaseTrigger implements EventTrigger {

    /**
     * The job's event queue.
     */
    protected BlockingQueue<Map<String, Object>> eventQueue;

    /**
     * Creates a new component instance.
     *
     * @param categoryId The category ID.
     * @param typeId     The type ID.
     */
    protected BaseEventTrigger(String categoryId, String typeId) {
        super(categoryId, typeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEventQueue(BlockingQueue<Map<String, Object>> queue) {
        this.eventQueue = queue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processEvent(Map<String, Object> eventData) {
        try {
            eventQueue.put(Objects.requireNonNullElseGet(eventData, HashMap::new));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IgorException("Interrupted during message processing!");
        }
    }

}
