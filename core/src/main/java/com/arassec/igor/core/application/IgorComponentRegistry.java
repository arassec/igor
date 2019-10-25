package com.arassec.igor.core.application;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.trigger.Trigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Registry for igor components. Keeps track of the component's types and categories.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IgorComponentRegistry implements InitializingBean {

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
     * Contains the categories of a certain component type (e.g. Action.class -> Action-Categories or Service.class ->
     * Service-Categories etc.).
     */
    private Map<Class, Set<String>> categoriesByComponentType = new HashMap<>();

    /**
     * Contains all available component types in a certain category (e.g. Category 'File-Actions'-ID -> 'List-Files'-ID,
     * 'Copy-File'-ID etc.).
     */
    private Map<String, Set<String>> typesByCategoryKey = new HashMap<>();

    /**
     * Contains the prototype of a specific igor component, indexed by its type.
     */
    private Map<String, IgorComponent> typeToComponentPrototype = new HashMap<>();

    /**
     * Contains a service interface and its category.
     */
    private Map<Class, String> serviceInterfaceToCategory = new HashMap<>();

    /**
     * Initializes the component registry.
     */
    @Override
    public void afterPropertiesSet() {
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
     * Returns the category ID of services implementing the given service interface.
     *
     * @param serviceInterface The interface of the services to get the category of.
     *
     * @return The category.
     */
    public String getCagetoryOfServiceInterface(Class serviceInterface) {
        if (serviceInterfaceToCategory.containsKey(serviceInterface)) {
            return serviceInterfaceToCategory.get(serviceInterface);
        }
        throw new IllegalArgumentException("Unknown service interface: " + serviceInterface);
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

            if (component instanceof Service) {
                Class<?>[] interfaces = ClassUtils.getAllInterfaces(component);
                if (interfaces != null && interfaces.length > 0) {
                    for (int i = 0; i < interfaces.length; i++) {
                        // Skip IgorComponent and Service
                        if (!interfaces[i].equals(IgorComponent.class) && !interfaces[i].equals(Service.class)) {
                            serviceInterfaceToCategory.put(interfaces[i], component.getCategoryId());
                        }
                    }
                }
            }

            categoriesByComponentType.get(componentType).add(component.getCategoryId());

            if (!typesByCategoryKey.containsKey(component.getCategoryId())) {
                typesByCategoryKey.put(component.getCategoryId(), new HashSet<>());
            }
            typesByCategoryKey.get(component.getCategoryId()).add(component.getTypeId());

            typeToComponentPrototype.put(component.getTypeId(), component);
        }
    }

}
