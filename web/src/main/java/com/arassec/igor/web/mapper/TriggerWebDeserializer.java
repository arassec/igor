package com.arassec.igor.web.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.repository.ServiceRepository;

import java.util.Map;

/**
 * Desrializer for {@link Trigger}s.
 */
public class TriggerWebDeserializer extends IgorComponentWebDeserializer<Trigger> {
    /**
     * Creates a new deserializer instance.
     *
     * @param igorComponentRegistry The component registry.
     * @param serviceRepository     The service repository to load services from.
     * @param simulationMode        Set to {@code true} if the resulting  components are used during simulated job runs.
     */
    public TriggerWebDeserializer(IgorComponentRegistry igorComponentRegistry, ServiceRepository serviceRepository, boolean simulationMode) {
        super(Trigger.class, igorComponentRegistry, serviceRepository, simulationMode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Trigger createInstance(String typeId, Map<String, Object> parameters, boolean simulationMode) {
        return igorComponentRegistry.getTriggerInstance(typeId, parameters);
    }

}
