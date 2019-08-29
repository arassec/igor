package com.arassec.igor.core.application.factory;

import com.arassec.igor.core.application.factory.util.ArtifactScanner;
import com.arassec.igor.core.application.factory.util.KeyLabelStore;
import com.arassec.igor.core.model.IgorCategory;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base class for Factories.
 */
@Slf4j
public abstract class ModelFactory<T> {

    /**
     * Base package for annotation scanning.
     * <p>
     * TODO: This should be a list of configuration parameters configurable vai GUI.
     */
    private static final String BASE_PACKAGE = "com.arassec.igor";

    /**
     * Name of the annotation class' label() method.
     */
    private static final String VALUE_METHOD_NAME = "value";

    /**
     * Contains the categories of models this factory provides.
     */
    protected Set<KeyLabelStore> categories;

    /**
     * Contains the types of models this factory provides.
     */
    protected Set<KeyLabelStore> types;

    /**
     * Contains the categories and their corresponding types.
     */
    private Map<String, Set<KeyLabelStore>> typesByCategory = new HashMap<>();

    /**
     * Contains the category for every type.
     */
    private Map<String, KeyLabelStore> categoryByType = new HashMap<>();

    /**
     * Creates a new {@link ModelFactory}.
     */
    ModelFactory() {
        Class<T> modelClass = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), ModelFactory.class);

        log.info("Registering classes for: {}", modelClass.getName());
        categories = findCategories(modelClass);
        types = findComponents(modelClass);

        types.forEach(type -> {
            KeyLabelStore category;
            try {
                category = findCategoryOfComponent(Class.forName(type.getKey()), categories);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
            if (!typesByCategory.containsKey(category.getKey())) {
                typesByCategory.put(category.getKey(), new HashSet<>());
            }
            typesByCategory.get(category.getKey()).add(type);
            categoryByType.put(type.getKey(), category);
        });
    }

    /**
     * Creates a new model instance of the provided type.
     *
     * @param type The model type to create.
     *
     * @return An instance of the model or null, if none could be created.
     */
    public T createInstance(String type) {
        try {
            return (T) Class.forName(type).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            log.error("Could not instantiate type: " + type, e);
        }
        return null;
    }

    /**
     * Creates a new model instance of the provided type and sets the parameters to the new instance.
     *
     * @param type       The model type to create.
     * @param parameters The model's parameters, that should be applied to the model.
     *
     * @return The model instance with cleartext properties
     */
    public synchronized T createInstance(String type, Map<String, Object> parameters) {
        T instance = createInstance(type);
        if (instance == null) {
            return null;
        }
        if (parameters != null && !parameters.isEmpty()) {
            ReflectionUtils.doWithFields(instance.getClass(), field -> {
                if (field.isAnnotationPresent(IgorParam.class) && parameters.containsKey(field.getName())) {
                    try {
                        field.setAccessible(true);
                        field.set(instance, parameters.get(field.getName()));
                        field.setAccessible(false);
                    } catch (IllegalAccessException e) {
                        log.error("Trying to create '{}' with parameter: {}", type, field.getName());
                        throw new IllegalStateException(e);
                    }
                }
            });
        }
        return instance;
    }

    /**
     * Returns the category of the provided instance.
     *
     * @param instance The instance to get the category for.
     *
     * @return The category definition or {@code null}, if none was found.
     */
    public KeyLabelStore getCategory(T instance) {
        String type = getType(instance).getKey();
        if (type != null && categoryByType.containsKey(type)) {
            return categoryByType.get(type);
        }
        return null;
    }

    /**
     * Returns the type of the supplied instance.
     *
     * @param instance The instance to get the type for.
     *
     * @return The type or {@code null}, if none exists for this instance.
     */
    public KeyLabelStore getType(T instance) {
        return types.stream().filter(modelDefinition -> modelDefinition.getKey().equals(instance.getClass().getName()))
                .findFirst().orElse(null);
    }

    /**
     * Finds the category of the supplied component in the set of available categories.
     *
     * @param modelClass The model class to find a definition for.
     * @param categories The available categories.
     *
     * @return A model definition for the supplied class.
     */
    private KeyLabelStore findCategoryOfComponent(Class modelClass, Set<KeyLabelStore> categories) {
        for (KeyLabelStore category : categories) {
            try {
                if (Class.forName(category.getKey()).isAssignableFrom(modelClass)) {
                    return category;
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalArgumentException("No model definition for class '" + modelClass.getName() + "' defined!");
    }

    /**
     * Registers {@link IgorCategory}s for the provided model class.
     *
     * @param modelClass The model class this factory provides.
     *
     * @return Set of {@link KeyLabelStore}s containing the model's categories.
     */
    private Set<KeyLabelStore> findCategories(Class<T> modelClass) {
        Set<KeyLabelStore> result = new HashSet<>();
        if (modelClass != null) {
            ClassPathScanningCandidateComponentProvider scanner = new ArtifactScanner(modelClass, IgorCategory.class, true);
            for (BeanDefinition beanDefinition : scanner.findCandidateComponents(BASE_PACKAGE)) {
                KeyLabelStore keyLabelStore = createKeyLabelStore(beanDefinition, IgorCategory.class);
                result.add(keyLabelStore);
                log.debug("Registered category: {} / {}", keyLabelStore.getLabel(), keyLabelStore.getKey());
            }
        }
        return result;
    }

    /**
     * Registers {@link IgorComponent} implementations for the provided model class.
     *
     * @param modelClass The model class this factory provides.
     *
     * @return Set of {@link KeyLabelStore}s containing the model's components.
     */
    private Set<KeyLabelStore> findComponents(Class<T> modelClass) {
        Set<KeyLabelStore> result = new HashSet<>();
        if (modelClass != null) {
            ClassPathScanningCandidateComponentProvider scanner = new ArtifactScanner(modelClass, IgorComponent.class, false);
            for (BeanDefinition beanDefinition : scanner.findCandidateComponents(BASE_PACKAGE)) {
                result.add(createKeyLabelStore(beanDefinition, IgorComponent.class));
            }
        }
        return result;
    }

    /**
     * Creates a {@link KeyLabelStore} for the supplied bean definition an annotation class.
     *
     * @param beanDefinition  The bean definition to take the data from.
     * @param annotationClass The annotation class to extract the label from.
     *
     * @return A {@link KeyLabelStore} for the supplied bean.
     */
    private KeyLabelStore createKeyLabelStore(BeanDefinition beanDefinition, Class<? extends Annotation> annotationClass) {
        try {
            Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());
            KeyLabelStore keyLabelStore = new KeyLabelStore();
            keyLabelStore.setKey(clazz.getName());
            keyLabelStore.setLabel(String.valueOf(annotationClass.getMethod(VALUE_METHOD_NAME).invoke(clazz.getAnnotation(annotationClass))));
            log.debug("Registered {} / '{}'", keyLabelStore.getKey(), keyLabelStore.getLabel());
            return keyLabelStore;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns the category definitions this factory provides.
     *
     * @return Set of category definitions.
     */
    public Set<KeyLabelStore> getCategories() {
        return categories;
    }

    /**
     * Returns the categories and their corresponding types.
     *
     * @return categories and their types.
     */
    public Map<String, Set<KeyLabelStore>> getTypesByCategory() {
        return typesByCategory;
    }

}
