package com.arassec.igor.web.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.web.simulation.ActionProxy;

import java.util.Map;

/**
 * Desrializer for {@link Action}s.
 */
public class ActionWebDeserializer extends IgorComponentWebDeserializer<Action> {
    /**
     * Creates a new deserializer instance.
     *
     * @param igorComponentRegistry The component registry.
     * @param serviceRepository     The service repository to load services from.
     * @param simulationMode        Set to {@code true} if the resulting  components are used during simulated job runs.
     */
    public ActionWebDeserializer(IgorComponentRegistry igorComponentRegistry, ServiceRepository serviceRepository, boolean simulationMode) {
        super(Action.class, igorComponentRegistry, serviceRepository, simulationMode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Action createInstance(String typeId, Map<String, Object> parameters, boolean simulationMode) {
        Action action = igorComponentRegistry.createActionInstance(typeId, parameters);
        if (simulationMode) {
            return new ActionProxy(action);
        } else {
            return action;
        }
    }

}
