package com.arassec.igor.web.mapper;

import com.arassec.igor.application.registry.IgorComponentRegistry;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.repository.ConnectorRepository;

import java.util.Map;

/**
 * Desrializer for {@link Action}s.
 */
public class ActionWebDeserializer extends IgorComponentWebDeserializer<Action> {
    /**
     * Creates a new deserializer instance.
     *
     * @param igorComponentRegistry The component registry.
     * @param connectorRepository   The connector repository to load connectors from.
     */
    public ActionWebDeserializer(IgorComponentRegistry igorComponentRegistry, ConnectorRepository connectorRepository) {
        super(Action.class, igorComponentRegistry, connectorRepository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Action createInstance(String typeId, Map<String, Object> parameters) {
        return igorComponentRegistry.createActionInstance(typeId, parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setComponentSpecifica(IgorComponent instance, Map<String, Object> componentJson) {
        super.setComponentSpecifica(instance, componentJson);
        ((Action) instance).setActive((boolean) componentJson.get(WebMapperKey.ACTIVE.getKey()));
        ((Action) instance).setName((String) componentJson.get(WebMapperKey.NAME.getKey()));
        ((Action) instance).setDescription((String) componentJson.get(WebMapperKey.DESCRIPTION.getKey()));
    }

}
