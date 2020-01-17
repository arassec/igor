package com.arassec.igor.persistence.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.persistence.security.SecurityProvider;

import java.util.Map;

public class ServicePersistenceDeserializer extends IgorComponentPersistenceDeserializer<Service> {

    /**
     * Creates a new deserializer.
     *
     * @param igorComponentRegistry The component registry.
     * @param securityProvider      The security provider to decrypt secured parameter values.
     */
    public ServicePersistenceDeserializer(IgorComponentRegistry igorComponentRegistry, SecurityProvider securityProvider) {
        super(Service.class, igorComponentRegistry, null, securityProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Service createInstance(String typeId, Map<String, Object> parameters) {
        return igorComponentRegistry.getServiceInstance(typeId, parameters);
    }

}