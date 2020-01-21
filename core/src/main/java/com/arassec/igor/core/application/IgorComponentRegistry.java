package com.arassec.igor.core.application;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.trigger.Trigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.util.*;

/**
 * Registry for igor components. Keeps track of the component's types and categories.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IgorComponentRegistry implements InitializingBean, ApplicationContextAware {

    /**
     * The Spring application context.
     */
    private ApplicationContext applicationContext;

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
    private final Map<Class<? extends IgorComponent>, Set<String>> categoriesByComponentType = new HashMap<>();

    /**
     * Contains all available component types in a certain category (e.g. Category 'File-Actions'-ID -> 'List-Files'-ID,
     * 'Copy-File'-ID etc.).
     */
    private final Map<String, Set<String>> typesByCategoryKey = new HashMap<>();

    /**
     * Contains a service interface and its category.
     */
    private final Map<Class<?>, String> serviceInterfaceToCategory = new HashMap<>();

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
     * Sets the application context to create new instances igor-component beans by their type-ID.
     *
     * @param applicationContext The Spring application context.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Returns the categories of a component type, e.g. all categories of {@link Action} components.
     *
     * @param componentType The type of component to get the categories of.
     *
     * @return Set of categories of the specified component type or an empty set, if none are available.
     */
    public Set<String> getCategoriesOfComponentType(Class<? extends IgorComponent> componentType) {
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
    public String getCagetoryOfServiceInterface(Class<?> serviceInterface) {
        if (serviceInterfaceToCategory.containsKey(serviceInterface)) {
            return serviceInterfaceToCategory.get(serviceInterface);
        }
        throw new IllegalArgumentException("Unknown service interface: " + serviceInterface);
    }

    /**
     * Returns a new {@link Action} instance for the given type ID.
     *
     * @param typeId     The action's type ID.
     * @param parameters The parameters of the newly created action.
     *
     * @return The new {@link Action} instance.
     */
    public Action createActionInstance(String typeId, Map<String, Object> parameters) {
        Optional<Action> optional = actions.stream().filter(action -> action.getTypeId().equals(typeId)).findFirst();
        if (optional.isPresent()) {
            Action action = applicationContext.getBean(optional.get().getClass());
            applyParameters(action, parameters);
            return action;
        }
        throw new IllegalArgumentException("No action found for type ID: " + typeId);
    }

    /**
     * Returns a new {@link Service} instance for the given type ID.
     *
     * @param typeId     The service's type ID.
     * @param parameters The parameters of the newly created action.
     *
     * @return The new {@link Service} instance.
     */
    public Service createServiceInstance(String typeId, Map<String, Object> parameters) {
        Optional<Service> optional = services.stream().filter(service -> service.getTypeId().equals(typeId)).findFirst();
        if (optional.isPresent()) {
            Service service = applicationContext.getBean(optional.get().getClass());
            applyParameters(service, parameters);
            return service;
        }
        throw new IllegalArgumentException("No service found for type ID: " + typeId);
    }

    /**
     * Returns a new {@link Provider} instance for the given type ID.
     *
     * @param typeId     The provider's type ID.
     * @param parameters The parameters of the newly created action.
     *
     * @return The new {@link Provider} instance.
     */
    public Provider createProviderInstance(String typeId, Map<String, Object> parameters) {
        Optional<Provider> optional = providers.stream().filter(provider -> provider.getTypeId().equals(typeId)).findFirst();
        if (optional.isPresent()) {
            Provider provider = applicationContext.getBean(optional.get().getClass());
            applyParameters(provider, parameters);
            return provider;
        }
        throw new IllegalArgumentException("No provider found for type ID: " + typeId);
    }

    /**
     * Returns a new {@link Trigger} instance for the given type ID.
     *
     * @param typeId     The trigger's type ID.
     * @param parameters The parameters of the newly created action.
     *
     * @return The new {@link Trigger} instance.
     */
    public Trigger createTriggerInstance(String typeId, Map<String, Object> parameters) {
        Optional<Trigger> optional = triggers.stream().filter(trigger -> trigger.getTypeId().equals(typeId)).findFirst();
        if (optional.isPresent()) {
            Trigger trigger = applicationContext.getBean(optional.get().getClass());
            applyParameters(trigger, parameters);
            return trigger;
        }
        throw new IllegalArgumentException("No trigger found for type ID: " + typeId);
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
                initializeServiceInterface(component);
            }

            categoriesByComponentType.get(componentType).add(component.getCategoryId());

            if (!typesByCategoryKey.containsKey(component.getCategoryId())) {
                typesByCategoryKey.put(component.getCategoryId(), new HashSet<>());
            }

            typesByCategoryKey.get(component.getCategoryId()).add(component.getTypeId());
        }
    }

    /**
     * Determines the base interface of a {@link Service} and saves the category ID of it.
     *
     * @param component The service.
     */
    private void initializeServiceInterface(IgorComponent component) {
        Class<?>[] interfaces = ClassUtils.getAllInterfaces(component);
        if (interfaces.length > 0) {
            for (Class<?> anInterface : interfaces) {
                // Skip IgorComponent and Service
                if (!anInterface.equals(IgorComponent.class) && !anInterface.equals(Service.class)) {
                    serviceInterfaceToCategory.put(anInterface, component.getCategoryId());
                }
            }
        }
    }

    /**
     * Sets the supplied parameters at the supplied instance.
     *
     * @param instance   The instnace to apply the parameters to.
     * @param parameters The parameters to set.
     */
    private void applyParameters(Object instance, Map<String, Object> parameters) {
        if (instance != null && parameters != null && !parameters.isEmpty()) {
            ReflectionUtils.doWithFields(instance.getClass(), field -> {
                if (parameters.containsKey(field.getName())) {
                    ReflectionUtils.makeAccessible(field);
                    ReflectionUtils.setField(field, instance, parameters.get(field.getName()));
                }
            });
        }
    }

}
