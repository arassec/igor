package com.arassec.igor.core.application;

import com.arassec.igor.core.application.factory.ActionFactory;
import com.arassec.igor.core.model.action.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Manages actions. Entry point from outside the core package to create and maintain actions.
 */
@Slf4j
@Component
public class ActionManager {

    /**
     * The factory for actions.
     */
    @Autowired
    private ActionFactory actionFactory;

    /**
     * Returns all available action types.
     *
     * @return Set of action types.
     */
    public Set<String> getTypes() {
        return actionFactory.getTypes();
    }

    /**
     * Creates an action with the given parameters.
     *
     * @param type       The action type to create.
     * @param parameters The values for the properties of the action that should be set.
     * @return An {@link Action} with all properties set according to the parameters.
     */
    public Action createAction(String type, Map<String, Object> parameters) {
        return actionFactory.createInstance(type, parameters, false);
    }

}
