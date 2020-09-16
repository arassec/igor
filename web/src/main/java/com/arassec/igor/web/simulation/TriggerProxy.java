package com.arassec.igor.web.simulation;

import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.trigger.EventTrigger;
import com.arassec.igor.core.model.trigger.Trigger;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Proxy for {@link Provider}s, that collects the provided data for later processing. Used during simulated job executions.
 */
@Getter
@Setter
public class TriggerProxy extends BaseProxy<Trigger> implements Trigger {

    /**
     * Creates a new instance.
     *
     * @param delegate The real trigger to use for method invocations.
     */
    public TriggerProxy(Trigger delegate) {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getMetaData() {
        return createMetaData(delegate.getMetaData());
    }

    /**
     * Returns the original trigger's data. If the trigger is event based, the configured simulation data is returned instead.
     *
     * @return The trigger's data or the trigger's simulation data, if it is an event based trigger.
     */
    @Override
    public Map<String, Object> getData() {
        if (delegate instanceof EventTrigger) {
            return ((EventTrigger) delegate).getSimulationData();
        }
        return delegate.getData();
    }

}
