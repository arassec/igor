package com.arassec.igor.persistence.mapper;

import com.arassec.igor.application.registry.IgorComponentRegistry;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.repository.ConnectorRepository;
import com.arassec.igor.persistence.security.SecurityProvider;

import java.util.Map;

public class TriggerPersistenceDeserializer extends IgorComponentPersistenceDeserializer<Trigger> {

    /**
     * Creates a new deserializer.
     *
     * @param igorComponentRegistry The component registry.
     * @param connectorRepository   The repository for connectors. Can be {@code null} to ignore connectors as parameter values.
     * @param securityProvider      The security provider to decrypt secured parameter values.
     */
    public TriggerPersistenceDeserializer(IgorComponentRegistry igorComponentRegistry, ConnectorRepository connectorRepository, SecurityProvider securityProvider) {
        super(Trigger.class, igorComponentRegistry, connectorRepository, securityProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Trigger createInstance(String typeId, Map<String, Object> parameters) {
        return igorComponentRegistry.createTriggerInstance(typeId, parameters);
    }

}
