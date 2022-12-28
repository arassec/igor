package com.arassec.igor.application.registry;

import com.arassec.igor.application.IgorApplicationProperties;
import com.arassec.igor.application.util.IgorComponentUtil;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.action.MissingComponentAction;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.connector.MissingComponentConnector;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.trigger.MissingComponentTrigger;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.util.IgorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Registry for igor components. Keeps track of the component's types and categories.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IgorComponentRegistry implements InitializingBean, ApplicationContextAware {

    /**
     * Set of builtin java object types.
     */
    private static final Set<Class<?>> javaObjectTypes = Set.of(Boolean.class, Byte.class, Short.class, Integer.class,
        Long.class, Float.class, Double.class, Character.class, String.class);

    /**
     * The Spring application context.
     */
    private ApplicationContext applicationContext;

    /**
     * All available actions.
     */
    private final List<Action> actions;

    /**
     * All available triggers.
     */
    private final List<Trigger> triggers;

    /**
     * All available connectors.
     */
    private final List<Connector> connectors;

    /**
     * Igor's application configuration properties.
     */
    private final IgorApplicationProperties igorApplicationProperties;

    /**
     * Utility for igor components.
     */
    private final IgorComponentUtil igorComponentUtil;

    /**
     * Contains the categories of a certain component type (e.g. Action.class -> Action-Categories or Connector.class ->
     * Connector-Categories etc.).
     */
    private final Map<Class<? extends IgorComponent>, Set<String>> categoriesByComponentType = new HashMap<>();

    /**
     * Contains all available type IDs for a certain action category.
     */
    private final Map<String, Set<String>> actionTypesByCategoryKey = new HashMap<>();

    /**
     * Contains all available type IDs for a certain trigger category.
     */
    private final Map<String, Set<String>> triggerTypesByCategoryKey = new HashMap<>();

    /**
     * Contains all available type IDs for a certain connector category.
     */
    private final Map<String, Set<String>> connectorTypesByCategoryKey = new HashMap<>();

    /**
     * Initializes the component registry.
     */
    @Override
    public void afterPropertiesSet() {
        initializeComponent(Action.class, actions, actionTypesByCategoryKey);
        initializeComponent(Trigger.class, triggers, triggerTypesByCategoryKey);
        initializeComponent(Connector.class, connectors, connectorTypesByCategoryKey);
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
     * Returns the action types of a specific category, e.g. all types of {@link Action}s for cateogry 'util'.
     *
     * @param categoryKey The category's key.
     *
     * @return Set of types or an empty set, if none are available.
     */
    public Set<String> getActionTypesOfCategory(String categoryKey) {
        if (actionTypesByCategoryKey.containsKey(categoryKey)) {
            return actionTypesByCategoryKey.get(categoryKey);
        }
        return Set.of();
    }

    /**
     * Returns the trigger types of a specific category, e.g. all types of {@link Trigger}s for cateogry 'util'.
     *
     * @param categoryKey The category's key.
     *
     * @return Set of types or an empty set, if none are available.
     */
    public Set<String> getTriggerTypesOfCategory(String categoryKey) {
        if (triggerTypesByCategoryKey.containsKey(categoryKey)) {
            return triggerTypesByCategoryKey.get(categoryKey);
        }
        return Set.of();
    }

    /**
     * Returns the connector types of a specific category, e.g. all types of {@link Connector}s for cateogry 'file'.
     *
     * @param categoryKey The category's key.
     *
     * @return Set of types or an empty set, if none are available.
     */
    public Set<String> getConnectorTypesOfCategory(String categoryKey) {
        if (connectorTypesByCategoryKey.containsKey(categoryKey)) {
            return connectorTypesByCategoryKey.get(categoryKey);
        }
        return Set.of();
    }

    /**
     * Returns a new {@link Job} instance as prototype.
     *
     * @return A new job instance.
     */
    public Job createJobPrototype() {
        var trigger = createTriggerInstance(igorComponentUtil.getTypeId(triggers.stream()
            .filter(triggerCandidate -> igorComponentUtil.getTypeId(triggerCandidate).equals(igorApplicationProperties.getDefaultTrigger()))
            .findFirst()
            .orElse(triggers.get(0))), null);
        trigger.setId(UUID.randomUUID().toString());

        return Job.builder()
            .id(UUID.randomUUID().toString())
            .name("New Job")
            .active(true)
            .trigger(trigger)
            .build();
    }

    /**
     * Returns a new {@link Action} instance as protopye.
     *
     * @return A new action instance.
     */
    public Action createActionPrototype() {
        var action = createActionInstance(igorComponentUtil.getTypeId(actions.stream()
            .filter(actionCandidate -> igorComponentUtil.getTypeId(actionCandidate).equals(igorApplicationProperties.getDefaultAction()))
            .findFirst()
            .orElse(actions.get(0))), null);
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
        Optional<Action> optional = actions.stream().filter(action -> igorComponentUtil.getTypeId(action).equals(typeId)).findFirst();
        if (optional.isPresent()) {
            var action = applicationContext.getBean(optional.get().getClass());
            applyParameters(action, parameters);
            return action;
        }
        return new MissingComponentAction("No action available for type ID: " + typeId);
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
        Optional<Connector> optional =
            connectors.stream().filter(connector -> igorComponentUtil.getTypeId(connector).equals(typeId)).findFirst();
        if (optional.isPresent()) {
            Connector connector = applicationContext.getBean(optional.get().getClass());
            applyParameters(connector, parameters);
            return connector;
        }
        return new MissingComponentConnector("No connector available for type ID: " + typeId);
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
        Optional<Trigger> optional =
            triggers.stream().filter(trigger -> igorComponentUtil.getTypeId(trigger).equals(typeId)).findFirst();
        if (optional.isPresent()) {
            Trigger trigger = applicationContext.getBean(optional.get().getClass());
            applyParameters(trigger, parameters);
            return trigger;
        }
        return new MissingComponentTrigger("No trigger available for type ID: " + typeId);
    }

    /**
     * Returns all valid categories and types of connectors that can be used as parameter values for the supplied parameter
     * class.
     *
     * @param parameterClass The class of the parameter.
     *
     * @return Map of components that can be assigned to the parameter.
     */
    public Map<String, Set<String>> getConnectorParameterCategoryAndType(Class<?> parameterClass) {
        Map<String, Set<String>> result = new HashMap<>();
        if (parameterClass != null) {
            connectors.stream()
                .filter(connector -> parameterClass.isAssignableFrom(connector.getClass()))
                .forEach(connector -> {
                    String categoryId = igorComponentUtil.getCategoryId(connector);
                    if (!result.containsKey(categoryId)) {
                        result.put(categoryId, new HashSet<>());
                    }
                    result.get(categoryId).add(igorComponentUtil.getTypeId(connector));
                });
        }
        return result;
    }

    /**
     * Initializes categories and types of a specific igor component, e.g. {@link Connector}.
     *
     * @param componentType       The type of the component to initialize.
     * @param components          List of components of that type (e.g. all Actions on the classpath).
     * @param typeByCategoryStore Map to store all component type IDs, indexed by category.
     */
    private void initializeComponent(Class<? extends IgorComponent> componentType, List<? extends IgorComponent> components,
                                     Map<String, Set<String>> typeByCategoryStore) {
        categoriesByComponentType.put(componentType, new HashSet<>());
        for (IgorComponent component : components) {
            String categoryId = igorComponentUtil.getCategoryId(component);
            categoriesByComponentType.get(componentType).add(categoryId);
            typeByCategoryStore.computeIfAbsent(categoryId, s -> new HashSet<>());
            typeByCategoryStore.get(categoryId).add(igorComponentUtil.getTypeId(component));
        }
    }

    /**
     * Sets the supplied parameters at the supplied instance.
     *
     * @param instance   The instance to apply the parameters to.
     * @param parameters The parameters to set.
     */
    private void applyParameters(Object instance, Map<String, Object> parameters) {
        if (instance != null) {
            ReflectionUtils.doWithFields(instance.getClass(), field -> {
                ReflectionUtils.makeAccessible(field);
                if (parameters != null && parameters.containsKey(field.getName())) {
                    Object parameterValue = parameters.get(field.getName());
                    if (parameterValue instanceof MissingComponentConnector) {
                        parameterValue = handleMissingConnector(field);
                    }
                    ReflectionUtils.setField(field, instance, parameterValue);
                } else if (field.isAnnotationPresent(IgorParam.class) && parameters != null && !parameters.containsKey(field.getName())) {
                    // In this case the user didn't configure the property. If it is a primitive type, the default value is used.
                    // If it is an object type, the value is set to null.
                    clearFieldValue(instance, field);
                }
            });
        }
    }

    /**
     * Creates a drop-in replacement for a missing connector. In case the original connector is not available any more (e.g. due
     * to a deleted plugin), this method returns a fitting connector mock, so that igor can display the problem to the user
     * instead of throwing an exception during startup.
     *
     * @param field The field that the replacement connector is used for.
     *
     * @return A proxy to fill the missing connector.
     */
    private Object handleMissingConnector(Field field) {
        try {
            List<Class<?>> implementations = new LinkedList<>();
            if (!field.getType().equals(Connector.class)) {
                implementations.add(Connector.class);
            }
            return new ByteBuddy()
                .subclass(field.getType())
                .implement(implementations)
                .method(ElementMatchers.any())
                .intercept(InvocationHandlerAdapter.of((proxy, method, args) -> switch (method.getName()) {
                    case "getName", "toString" -> "Missing Connector!";
                    case "equals" -> false;
                    case "hashCode" -> 1;
                    default -> null;
                }))
                .make()
                .load(this.getClass().getClassLoader())
                .getLoaded()
                .getDeclaredConstructor()
                .newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IgorException("Could not process missing connector component!", e);
        }
    }

    /**
     * Clears the field's default value if it is a java object type.
     *
     * @param instance The target object to clear the field value on.
     * @param field    The property to set to {@code null}.
     */
    private void clearFieldValue(Object instance, Field field) {
        if (javaObjectTypes.contains(field.getType())) {
            ReflectionUtils.setField(field, instance, null);
        }
    }

}
