package com.arassec.igor.persistence.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.persistence.security.SecurityProvider;

import java.util.Map;

public class TriggerPersistenceDeserializer extends IgorComponentPersistenceDeserializer<Trigger> {

    /**
     * Creates a new deserializer.
     *
     * @param igorComponentRegistry The component registry.
     * @param serviceRepository     The repository for services. Can be {@code null} to ignore services as parameter values.
     * @param securityProvider      The security provider to decrypt secured parameter values.
     */
    public TriggerPersistenceDeserializer(IgorComponentRegistry igorComponentRegistry, ServiceRepository serviceRepository, SecurityProvider securityProvider) {
        super(Trigger.class, igorComponentRegistry, serviceRepository, securityProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Trigger createInstance(String typeId, Map<String, Object> parameters) {
        return igorComponentRegistry.createTriggerInstance(typeId, parameters);
    }

}
