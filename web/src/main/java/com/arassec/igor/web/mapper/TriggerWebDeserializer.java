package com.arassec.igor.web.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.repository.ConnectorRepository;
import com.arassec.igor.web.simulation.TriggerProxy;

import java.util.Map;

/**
 * Desrializer for {@link Trigger}s.
 */
public class TriggerWebDeserializer extends IgorComponentWebDeserializer<Trigger> {
    /**
     * Creates a new deserializer instance.
     *
     * @param igorComponentRegistry The component registry.
     * @param connectorRepository   The connector repository to load connectors from.
     * @param simulationMode        Set to {@code true} if the resulting  components are used during simulated job runs.
     */
    public TriggerWebDeserializer(IgorComponentRegistry igorComponentRegistry, ConnectorRepository connectorRepository, boolean simulationMode) {
        super(Trigger.class, igorComponentRegistry, connectorRepository, simulationMode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Trigger createInstance(String typeId, Map<String, Object> parameters, boolean simulationMode) {
        Trigger trigger = igorComponentRegistry.createTriggerInstance(typeId, parameters);
        if (simulationMode) {
            return new TriggerProxy(trigger);
        } else {
            return trigger;
        }
    }

}
