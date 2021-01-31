package com.arassec.igor.web.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.repository.ConnectorRepository;

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
     */
    public TriggerWebDeserializer(IgorComponentRegistry igorComponentRegistry, ConnectorRepository connectorRepository) {
        super(Trigger.class, igorComponentRegistry, connectorRepository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Trigger createInstance(String typeId, Map<String, Object> parameters) {
        return igorComponentRegistry.createTriggerInstance(typeId, parameters);
    }

}
