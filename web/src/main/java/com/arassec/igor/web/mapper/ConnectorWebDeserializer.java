package com.arassec.igor.web.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
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
     * @param simulationMode        Set to {@code true} if the resulting  components are used during simulated job runs.
     */
    public ConnectorWebDeserializer(IgorComponentRegistry igorComponentRegistry, boolean simulationMode) {
        super(Connector.class, igorComponentRegistry, null, simulationMode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Connector createInstance(String typeId, Map<String, Object> parameters, boolean simulationMode) {
        return igorComponentRegistry.createConnectorInstance(typeId, parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setComponentSpecifica(IgorComponent instance, Map<String, Object> map) {
        super.setComponentSpecifica(instance, map);
        if (map.containsKey(WebMapperKey.ID.getKey())) {
            instance.setId(String.valueOf(map.get(WebMapperKey.ID.getKey())));
        }
        ((Connector) instance).setName(String.valueOf(map.get(WebMapperKey.NAME.getKey())));
    }

}
