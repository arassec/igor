package com.arassec.igor.core.application;

import com.arassec.igor.core.application.factory.ModelFactory;
import com.arassec.igor.core.application.factory.util.KeyLabelStore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base class for managers that handle igor models.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class IgorComponentManager<T> {

    /**
     * The factory for the model.
     */
    @Getter
    private final ModelFactory<T> modelFactory;

    /**
     * Returns all available categories.
     *
     * @return The categories.
     */
    public Set<KeyLabelStore> getCategories() {
        return modelFactory.getCategories();
    }

    /**
     * Returns all available types of the specified category.
     *
     * @return The types of the specified category.
     */
    public Set<KeyLabelStore> getTypesOfCategory(String categoryType) {
        if (modelFactory.getTypesByCategory().containsKey(categoryType)) {
            return modelFactory.getTypesByCategory().get(categoryType);
        }
        return new HashSet<>();
    }

    /**
     * Creates a new instance of the given type with the given configuration.
     *
     * @param type       The type.
     * @param parameters The property values to set in the instance.
     *
     * @return A newly created model instance with the given configuration.
     */
    public T createInstance(String type, Map<String, Object> parameters) {
        return modelFactory.createInstance(type, parameters);
    }

}
