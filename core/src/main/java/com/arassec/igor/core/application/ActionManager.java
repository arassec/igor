package com.arassec.igor.core.application;

import com.arassec.igor.core.application.factory.ActionFactory;
import com.arassec.igor.core.application.factory.util.KeyLabelStore;
import com.arassec.igor.core.model.action.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
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
     * Returns all available service categories.
     *
     * @return The categories.
     */
    public Set<KeyLabelStore> getCategories() {
        return actionFactory.getCategories();
    }

    /**
     * Returns all available types of services of the specified category.
     *
     * @return The service types of the specified category.
     */
    public Set<KeyLabelStore> getTypesOfCategory(String categoryType) {
        if (actionFactory.getTypesByCategory().containsKey(categoryType)) {
            return actionFactory.getTypesByCategory().get(categoryType);
        }
        return new HashSet<>();
    }

    /**
     * Creates an action with the given parameters.
     *
     * @param type       The action type to create.
     * @param parameters The values for the properties of the action that should be set.
     * @return An {@link Action} with all properties set according to the parameters.
     */
    public Action createAction(String type, Map<String, Object> parameters) {
        return actionFactory.createInstance(type, parameters);
    }

}
