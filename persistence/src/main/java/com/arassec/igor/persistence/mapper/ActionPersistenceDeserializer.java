package com.arassec.igor.persistence.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.repository.ConnectorRepository;
import com.arassec.igor.persistence.security.SecurityProvider;

import java.util.Map;

/**
 * Desrializer for {@link Action}s.
 */
public class ActionPersistenceDeserializer extends IgorComponentPersistenceDeserializer<Action> {

    /**
     * Creates a new deserializer.
     *
     * @param igorComponentRegistry The component registry.
     * @param connectorRepository   The repository for connectors. Can be {@code null} to ignore connectors as parameter values.
     * @param securityProvider      The security provider to decrypt secured parameter values.
     */
    public ActionPersistenceDeserializer(IgorComponentRegistry igorComponentRegistry, ConnectorRepository connectorRepository, SecurityProvider securityProvider) {
        super(Action.class, igorComponentRegistry, connectorRepository, securityProvider);
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
        if (componentJson.containsKey(PersistenceMapperKey.ACTIVE.getKey())) {
            ((Action) instance).setActive((boolean) componentJson.get(PersistenceMapperKey.ACTIVE.getKey()));
        }
        if (componentJson.containsKey(PersistenceMapperKey.NAME.getKey())) {
            ((Action) instance).setName((String) componentJson.get(PersistenceMapperKey.NAME.getKey()));
        }
        if (componentJson.containsKey(PersistenceMapperKey.DESCRIPTION.getKey())) {
            ((Action) instance).setDescription((String) componentJson.get(PersistenceMapperKey.DESCRIPTION.getKey()));
        }
    }
}
