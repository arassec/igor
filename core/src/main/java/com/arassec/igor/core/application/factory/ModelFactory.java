package com.arassec.igor.core.application.factory;

import com.arassec.igor.core.application.factory.util.CategoryScanner;
import com.arassec.igor.core.application.factory.util.KeyLabelStore;
import com.arassec.igor.core.model.IgorParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
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
     * TODO: This should be determined by e.g. an Igorfile or a list of configuration parameters.
     */
    private static final String BASE_PACKAGE = "com.arassec.igor";

    /**
     * Name of the annotation class' label() method.
     */
    private static final String LABEL_METHOD_NAME = "label";

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
     *
     * @param modelClass              The model class this factory provides, e.g. 'Service'.
     * @param categoryAnnotationClass The annotation that defines categories for the models of this factory, e.g. 'IgorServiceCategory'.
     * @param typeAnnotationClass     The annotation that marks a class as type for this factory, e.g. 'IgorService'.
     */
    public ModelFactory(Class<T> modelClass, Class<? extends Annotation> categoryAnnotationClass, Class<? extends Annotation> typeAnnotationClass) {
        log.info("Registering categories using: {}", categoryAnnotationClass.getName());
        categories = findModelDefinitions(modelClass, categoryAnnotationClass, true);

        log.info("Registering types using: {}", typeAnnotationClass.getName());
        types = findModelDefinitions(modelClass, typeAnnotationClass, false);

        types.stream().forEach(type -> {
            KeyLabelStore category;
            try {
                category = findModelDefintion(Class.forName(type.getKey()), categories);
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
     * @return The type or {@code null}, if none exists for this instance.
     */
    public KeyLabelStore getType(T instance) {
        return types.stream().filter(modelDefinition -> modelDefinition.getKey().equals(instance.getClass().getName()))
                .findFirst().orElse(null);
    }

    /**
     * Finds the model definition for the supplied class in the set of supplied model definitions.
     *
     * @param modelClass       The model class to find a definition for.
     * @param keyLabelStores The available model definitions.
     * @return A model definition for the supplied class.
     */
    private KeyLabelStore findModelDefintion(Class modelClass, Set<KeyLabelStore> keyLabelStores) {
        for (KeyLabelStore keyLabelStore : keyLabelStores) {
            try {
                if (Class.forName(keyLabelStore.getKey()).isAssignableFrom(modelClass)) {
                    return keyLabelStore;
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalArgumentException("No model definition for class '" + modelClass.getName() + "' defined!");
    }

    /**
     * Registers model definitions for the provided model and annotation class.
     *
     * @param modelClass      The model class this factory provides.
     * @param annotationClass The annotation that defines the model definition, e.g. a type or category annotation.
     * @return Set of {@link KeyLabelStore}s.
     */
    private Set<KeyLabelStore> findModelDefinitions(Class<T> modelClass, Class<? extends Annotation> annotationClass, boolean searchCategories) {
        Set<KeyLabelStore> result = new HashSet<>();

        if (modelClass != null && annotationClass != null) {
            ClassPathScanningCandidateComponentProvider scanner;
            if (searchCategories) {
                scanner = new CategoryScanner(annotationClass);
            } else {
                scanner = new ClassPathScanningCandidateComponentProvider(false);
                scanner.addIncludeFilter(new AnnotationTypeFilter(annotationClass));
            }

            for (BeanDefinition beanDefinition : scanner.findCandidateComponents(BASE_PACKAGE)) {
                try {
                    Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());
                    if (searchCategories || modelClass.isAssignableFrom(clazz)) {
                        KeyLabelStore keyLabelStore = new KeyLabelStore();
                        keyLabelStore.setKey(clazz.getName());
                        keyLabelStore.setLabel(String.valueOf(annotationClass.getMethod(LABEL_METHOD_NAME).invoke(clazz.getAnnotation(annotationClass))));
                        log.debug("Registered {} / '{}'", keyLabelStore.getKey(), keyLabelStore.getLabel());
                        result.add(keyLabelStore);
                    }
                } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        return result;
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
     * Returns the type definitions this factory provides.
     *
     * @return Set of type definitions.
     */
    public Set<KeyLabelStore> getTypes() {
        return types;
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
