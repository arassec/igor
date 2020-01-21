package com.arassec.igor.persistence.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.repository.ServiceRepository;
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
     * @param serviceRepository     The repository for services. Can be {@code null} to ignore services as parameter values.
     * @param securityProvider      The security provider to decrypt secured parameter values.
     */
    public ActionPersistenceDeserializer(IgorComponentRegistry igorComponentRegistry, ServiceRepository serviceRepository, SecurityProvider securityProvider) {
        super(Action.class, igorComponentRegistry, serviceRepository, securityProvider);
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
    protected void setComponentSpecifica(IgorComponent instance, Map<String, Object> map) {
        super.setComponentSpecifica(instance, map);
        if (map.containsKey(PersistenceMapperKey.ACTIVE.getKey())) {
            ((Action) instance).setActive((boolean) map.get(PersistenceMapperKey.ACTIVE.getKey()));
        }
    }
}
