package com.arassec.igor.core.application;

import com.arassec.igor.core.application.factory.TriggerFactory;
import com.arassec.igor.core.application.factory.util.KeyLabelStore;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.trigger.Trigger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages {@link Trigger}s. Entry point from outside the core package to create and maintain triggers.
 */
@Slf4j
@Component
public class TriggerManager {

    /**
     * The factory for triggers.
     */
    @Autowired
    private TriggerFactory triggerFactory;

    /**
     * Returns all available trigger categories.
     *
     * @return The categories.
     */
    public Set<KeyLabelStore> getCategories() {
        return triggerFactory.getCategories();
    }

    /**
     * Returns all available types of triggers of the specified category.
     *
     * @return The trigger types of the specified category.
     */
    public Set<KeyLabelStore> getTypesOfCategory(String categoryType) {
        if (triggerFactory.getTypesByCategory().containsKey(categoryType)) {
            return triggerFactory.getTypesByCategory().get(categoryType);
        }
        return new HashSet<>();
    }

    /**
     * Creates a provider of the given type with the given configuration.
     *
     * @param type       The provider's type.
     * @param parameters The property values to set in the provider instance.
     * @return A newly created {@link Provider} with the given configuration.
     */
    public Trigger createTrigger(String type, Map<String, Object> parameters) {
        return triggerFactory.createInstance(type, parameters);
    }

}
