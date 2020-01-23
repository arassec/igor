package com.arassec.igor.web.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.trigger.Trigger;

import java.util.Map;

/**
 * Desrializer for {@link Trigger}s.
 */
public class ServiceWebDeserializer extends IgorComponentWebDeserializer<Service> {
    /**
     * Creates a new deserializer instance.
     *
     * @param igorComponentRegistry The component registry.
     * @param simulationMode        Set to {@code true} if the resulting  components are used during simulated job runs.
     */
    public ServiceWebDeserializer(IgorComponentRegistry igorComponentRegistry, boolean simulationMode) {
        super(Service.class, igorComponentRegistry, null, simulationMode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Service createInstance(String typeId, Map<String, Object> parameters, boolean simulationMode) {
        return igorComponentRegistry.createServiceInstance(typeId, parameters);
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
        ((Service) instance).setName(String.valueOf(map.get(WebMapperKey.NAME.getKey())));
    }

}
