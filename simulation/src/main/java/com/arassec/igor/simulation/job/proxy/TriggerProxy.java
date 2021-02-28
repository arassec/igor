package com.arassec.igor.simulation.job.proxy;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.trigger.Trigger;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
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
    public Map<String, Object> createDataItem() {
        Map<String, Object> dataItem = getDataItem();

        // Clone the initial data item because it might get modified by actions:
        collectedData.add(getDataItem());

        return dataItem;
    }

    /**
     * Returns the initial data item.
     *
     * @return The initial data item.
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> getDataItem() {
        Map<String, Object> dataItem = delegate.createDataItem();
        if (!dataItem.containsKey(DataKey.META.getKey())) {
            dataItem.put(DataKey.META.getKey(), new HashMap<String, Object>());
        }
        ((Map<String, Object>) dataItem.get(DataKey.META.getKey())).put(DataKey.SIMULATION.getKey(), true);
        return dataItem;
    }

}
