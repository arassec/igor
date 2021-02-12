package com.arassec.igor.web.mapper;

import com.arassec.igor.application.registry.IgorComponentRegistry;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.connector.Connector;

import java.util.Map;

/**
 * Desrializer for {@link Connector}s.
 */
public class ConnectorWebDeserializer extends IgorComponentWebDeserializer<Connector> {

    /**
     * Creates a new deserializer instance.
     *
     * @param igorComponentRegistry The component registry.
     */
    public ConnectorWebDeserializer(IgorComponentRegistry igorComponentRegistry) {
        super(Connector.class, igorComponentRegistry, null);
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
        if (componentJson.containsKey(WebMapperKey.ID.getKey())) {
            instance.setId(String.valueOf(componentJson.get(WebMapperKey.ID.getKey())));
        }
        ((Connector) instance).setName(String.valueOf(componentJson.get(WebMapperKey.NAME.getKey())));
    }

}
