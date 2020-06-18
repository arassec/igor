package com.arassec.igor.core.application;

import com.arassec.igor.core.IgorCoreProperties;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.trigger.Trigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
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
     * All available connectors.
     */
    private final List<Connector> connectors;

    /**
     * Igor's core configuration properties.
     */
    private final IgorCoreProperties igorCoreProperties;

    /**
     * Contains the categories of a certain component type (e.g. Action.class -> Action-Categories or Connector.class ->
     * Connector-Categories etc.).
     */
    private final Map<Class<? extends IgorComponent>, Set<String>> categoriesByComponentType = new HashMap<>();

    /**
     * Contains all available component types in a certain category (e.g. Category 'File-Actions'-ID -> 'List-Files'-ID,
     * 'Copy-File'-ID etc.).
     */
    private final Map<String, Set<String>> typesByCategoryKey = new HashMap<>();

    /**
     * Initializes the component registry.
     */
    @Override
    public void afterPropertiesSet() {
        initializeComponent(Action.class, actions);
        initializeComponent(Provider.class, providers);
        initializeComponent(Trigger.class, triggers);
        initializeComponent(Connector.class, connectors);
    }

    /**
     * Sets the application context to create new instances igor-component beans by their type-ID.
     *
     * @param applicationContext The Spring application context.
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
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
     * Returns a new {@link Job} instance as prototype.
     *
     * @return A new job instance.
     */
    public Job createJobPrototype() {
        Trigger trigger = createTriggerInstance(triggers.stream()
                .filter(triggerCandidate -> triggerCandidate.getTypeId().equals(igorCoreProperties.getDefaultTrigger()))
                .findFirst()
                .orElse(triggers.get(0)).getTypeId(), null);
        trigger.setId(UUID.randomUUID().toString());

        Provider provider = createProviderInstance(providers.stream()
                .filter(providerCandidate -> providerCandidate.getTypeId().equals(igorCoreProperties.getDefaultProvider()))
                .findFirst()
                .orElse(providers.get(0)).getTypeId(), null);
        provider.setId(UUID.randomUUID().toString());

        return Job.builder()
                .id(UUID.randomUUID().toString())
                .name("New Job")
                .active(true)
                .trigger(trigger)
                .provider(provider)
                .build();
    }

    /**
     * Returns a new {@link Action} instance as protopye.
     *
     * @return A new action instance.
     */
    public Action createActionPrototype() {
        Action action = createActionInstance(actions.stream()
                .filter(actionCandidate -> actionCandidate.getTypeId().equals(igorCoreProperties.getDefaultAction()))
                .findFirst()
                .orElse(actions.get(0)).getTypeId(), null);
        action.setId(UUID.randomUUID().toString());
        return action;
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
     * Returns a new {@link Connector} instance for the given type ID.
     *
     * @param typeId     The connector's type ID.
     * @param parameters The parameters of the newly created action.
     *
     * @return The new {@link Connector} instance.
     */
    public Connector createConnectorInstance(String typeId, Map<String, Object> parameters) {
        Optional<Connector> optional = connectors.stream().filter(connector -> connector.getTypeId().equals(typeId)).findFirst();
        if (optional.isPresent()) {
            Connector connector = applicationContext.getBean(optional.get().getClass());
            applyParameters(connector, parameters);
            return connector;
        }
        throw new IllegalArgumentException("No connector found for type ID: " + typeId);
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
     * Returns all valid categories and types of connectors that can be used as parameter values for the supplied parameter
     * class.
     *
     * @param parameterClass The class of the parameter.
     *
     * @return List of components that can be assigned to the parameter.
     */
    public Map<String, Set<String>> getConnectorParameterCategoryAndType(Class<?> parameterClass) {
        Map<String, Set<String>> result = new HashMap<>();
        if (parameterClass != null) {
            connectors.stream()
                    .filter(connector -> parameterClass.isAssignableFrom(connector.getClass()))
                    .forEach(connector -> {
                        if (!result.containsKey(connector.getCategoryId())) {
                            result.put(connector.getCategoryId(), new HashSet<>());
                        }
                        result.get(connector.getCategoryId()).add(connector.getTypeId());
                    });
        }
        return result;
    }

    /**
     * Initializes categories and types of a specific igor component, e.g. {@link Connector}.
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
        }
    }

    /**
     * Sets the supplied parameters at the supplied instance.
     *
     * @param instance   The instance to apply the parameters to.
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
