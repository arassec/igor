package com.arassec.igor.simulation.job.proxy;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.trigger.Trigger;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Proxy for {@link Trigger}s. Used during simulated job executions.
 */
@Getter
@Setter
public class TriggerProxy extends BaseProxy<Trigger> implements Trigger {

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
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getMetaData() {
        return createMetaData(delegate.getMetaData());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getData() {
        return delegate.getData();
    }

    /**
     * Returns the triggers simulation data.
     *
     * @return List of data items created by the trigger.
     */
    public List<Map<String, Object>> getSimulationTriggerData() {
        return List.of(delegate.getData());
    }

    /**
     * Marks data items as "in simulation mode".
     *
     * @param delegateMetaData The delegate's meta-data.
     *
     * @return Meta-data for the data items.
     */
    private Map<String, Object> createMetaData(Map<String, Object> delegateMetaData) {
        Map<String, Object> metaData = new HashMap<>();
        metaData.put(DataKey.SIMULATION.getKey(), true);
        if (delegateMetaData != null) {
            delegateMetaData.forEach(metaData::put);
        }
        return metaData;
    }

}
