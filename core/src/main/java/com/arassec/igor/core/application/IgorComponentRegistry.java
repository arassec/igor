package com.arassec.igor.core.application;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.trigger.Trigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Registry for igor components. Keeps track of the component's types and categories.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IgorComponentRegistry {

    /**
     * All available actions.
     */
    private final List<Action> actions;

    /**
     * All available providers.
     */
    private final List<Provider> providers;

    /**
     * All available triggers.
     */
    private final List<Trigger> triggers;

    /**
     * All available services.
     */
    private final List<Service> services;

    /**
     * Contains the categories of a certain component type (e.g. Action or Service etc.).
     */
    private Map<Class, Set<String>> categoriesByComponentType = new HashMap<>();

    /**
     * Contains all available component types in a certain category.
     */
    private Map<String, Set<String>> typesByCategoryKey = new HashMap<>();

    /**
     * Contains the prototype of a specific igor component, indexed by its type.
     */
    private Map<String, IgorComponent> typeToComponentPrototype = new HashMap<>();

    /**
     * Initializes the component registry.
     */
    @PostConstruct
    public void initialize() {
        initializeComponent(Action.class, actions);
        initializeComponent(Provider.class, providers);
        initializeComponent(Trigger.class, triggers);
        initializeComponent(Service.class, services);
    }

    /**
     * Returns the categories of a component type, e.g. all categories of {@link Action} components.
     *
     * @param componentType The type of component to get the categories of.
     *
     * @return Set of categories of the specified component type or an empty set, if none are available.
     */
    public Set<String> getCategoriesOfComponentType(Class componentType) {
        if (categoriesByComponentType.containsKey(componentType)) {
            return categoriesByComponentType.get(componentType);
        }
        return Set.of();
    }

    /**
     * Returns the types of a specific category of components, e.g. all types of {@link Trigger}s.
     *
     * @param categoryKey The category's key.
     *
     * @return Set of types or an empty set, if none are available.
     */
    public Set<String> getTypesOfCategory(String categoryKey) {
        if (typesByCategoryKey.containsKey(categoryKey)) {
            return typesByCategoryKey.get(categoryKey);
        }
        return Set.of();
    }

    /**
     * Returns the class of the component with the provided type ID.
     *
     * @param typeId The component's type ID.
     *
     * @return The component's class if available.
     */
    public Optional<IgorComponent> getClass(String typeId) {
        if (typeToComponentPrototype.containsKey(typeId)) {
            return Optional.of(typeToComponentPrototype.get(typeId));
        }
        return Optional.empty();
    }

    /**
     * Initializes categories and types of a specific igor component, e.g. {@link Service}.
     *
     * @param componentType The type of the component to initialize.
     */
    private void initializeComponent(Class<? extends IgorComponent> componentType, List<? extends IgorComponent> components) {
        categoriesByComponentType.put(componentType, new HashSet<>());

        for (IgorComponent component : components) {
            categoriesByComponentType.get(componentType).add(component.getCategoryId());

            if (!typesByCategoryKey.containsKey(component.getCategoryId())) {
                typesByCategoryKey.put(component.getCategoryId(), new HashSet<>());
            }
            typesByCategoryKey.get(component.getCategoryId()).add(component.getTypeId());

            typeToComponentPrototype.put(component.getTypeId(), component);
        }
    }

}
