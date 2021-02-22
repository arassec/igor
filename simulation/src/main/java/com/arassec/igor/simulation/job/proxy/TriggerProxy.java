package com.arassec.igor.simulation.job.proxy;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.util.IgorException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Proxy for {@link Trigger}s. Used during simulated job executions.
 */
@Getter
@Setter
public class TriggerProxy extends BaseProxy<Trigger> implements Trigger {

    /**
     * The collected Data.
     */
    private List<Map<String, Object>> collectedData = new LinkedList<>();

    /**
     * Creates a new instance.
     *
     * @param delegate        The real trigger to use for method invocations.
     * @param simulationLimit The maximum number of data items that should be processed in a simulated job execution.
     */
    public TriggerProxy(Trigger delegate, int simulationLimit) {
        super(delegate, simulationLimit);
    }

    /**
     * Sets the simulation-key in the data item's meta data to {@code true}.
     *
     * @return The initial data item.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> createDataItem() {
        Map<String, Object> dataItem = delegate.createDataItem();
        ((Map<String, Object>) dataItem.get(DataKey.META.getKey())).put(DataKey.SIMULATION.getKey(), true);

        // Clone the initial data item because it might get modified by actions:
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            collectedData.add(objectMapper.readValue(objectMapper.writeValueAsString(dataItem), new TypeReference<>() {}));
        } catch (JsonProcessingException e) {
            throw new IgorException("Could not clone initial data item!", e);
        }

        return dataItem;
    }

}
