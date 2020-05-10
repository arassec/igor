package com.arassec.igor.persistence.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.repository.ConnectorRepository;
import com.arassec.igor.persistence.security.SecurityProvider;

import java.util.Map;

public class ProviderPersistenceDeserializer extends IgorComponentPersistenceDeserializer<Provider> {

    /**
     * Creates a new deserializer.
     *
     * @param igorComponentRegistry The component registry.
     * @param connectorRepository   The repository for connectors. Can be {@code null} to ignore connectors as parameter values.
     * @param securityProvider      The security provider to decrypt secured parameter values.
     */
    public ProviderPersistenceDeserializer(IgorComponentRegistry igorComponentRegistry, ConnectorRepository connectorRepository, SecurityProvider securityProvider) {
        super(Provider.class, igorComponentRegistry, connectorRepository, securityProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Provider createInstance(String typeId, Map<String, Object> parameters) {
        return igorComponentRegistry.createProviderInstance(typeId, parameters);
    }

}
