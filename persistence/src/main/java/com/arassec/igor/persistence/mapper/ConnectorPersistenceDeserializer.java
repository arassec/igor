package com.arassec.igor.persistence.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.persistence.security.SecurityProvider;

import java.util.Map;

public class ConnectorPersistenceDeserializer extends IgorComponentPersistenceDeserializer<Connector> {

    /**
     * Creates a new deserializer.
     *
     * @param igorComponentRegistry The component registry.
     * @param securityProvider      The security provider to decrypt secured parameter values.
     */
    public ConnectorPersistenceDeserializer(IgorComponentRegistry igorComponentRegistry, SecurityProvider securityProvider) {
        super(Connector.class, igorComponentRegistry, null, securityProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Connector createInstance(String typeId, Map<String, Object> parameters) {
        return igorComponentRegistry.createConnectorInstance(typeId, parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setComponentSpecifica(IgorComponent instance, Map<String, Object> componentJson) {
        super.setComponentSpecifica(instance, componentJson);
        ((Connector) instance).setName(String.valueOf(componentJson.get(PersistenceMapperKey.NAME.getKey())));
    }
}
