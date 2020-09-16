package com.arassec.igor.core.model.trigger;

import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

/**
 * Base for event based triggers. Provides sensible default implementations for most event based triggers.
 */
public abstract class BaseEventTrigger extends BaseTrigger implements EventTrigger {

    /**
     * Contains the user configured event data that is used during simulated job executions.
     */
    @Getter
    @Setter
    @IgorParam(subtype = ParameterSubtype.MULTI_LINE)
    private String simulationInput;

    /**
     * The job's event queue.
     */
    protected Queue<Map<String, Object>> eventQueue;

    /**
     * Creates a new component instance.
     *
     * @param categoryId The category ID.
     * @param typeId     The type ID.
     */
    public BaseEventTrigger(String categoryId, String typeId) {
        super(categoryId, typeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEventQueue(Queue<Map<String, Object>> queue) {
        this.eventQueue = queue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processEvent(Map<String, Object> eventData) {
        eventQueue.add(Objects.requireNonNullElseGet(eventData, Map::of));
    }

    /**
     * Returns the simulation data entered by the user. The entered data is processed line by line and expected to be in the form
     * of 'key=value' pairs. Data not matching that pattern is ignored.
     *
     * @return A map of simulation data.
     */
    @Override
    public Map<String, Object> getSimulationData() {
        if (StringUtils.hasText(simulationInput)) {
            Map<String, Object> triggerData = new HashMap<>();
            String[] dataParts = simulationInput.split("\n");
            for (String dataPart : dataParts) {
                String[] keyValue = dataPart.split("=");
                if (keyValue.length == 2) {
                    triggerData.put(keyValue[0], keyValue[1]);
                }
            }
            getData().forEach(triggerData::put); // Add custom trigger's data.
            return triggerData;
        }
        return getData();
    }

}
