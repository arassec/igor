package com.arassec.igor.persistence.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.persistence.security.SecurityProvider;

import java.util.Map;

public class ProviderPersistenceDeserializer extends IgorComponentPersistenceDeserializer<Provider> {

    /**
     * Creates a new deserializer.
     *
     * @param igorComponentRegistry The component registry.
     * @param serviceRepository     The repository for services. Can be {@code null} to ignore services as parameter values.
     * @param securityProvider      The security provider to decrypt secured parameter values.
     */
    public ProviderPersistenceDeserializer(IgorComponentRegistry igorComponentRegistry, ServiceRepository serviceRepository, SecurityProvider securityProvider) {
        super(Provider.class, igorComponentRegistry, serviceRepository, securityProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Provider createInstance(String typeId, Map<String, Object> parameters) {
        return igorComponentRegistry.createProviderInstance(typeId, parameters);
    }

}
