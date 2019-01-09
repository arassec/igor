package com.arassec.igor.core.application.factory;

import com.arassec.igor.core.application.util.EncryptionUtil;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.service.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
     * TODO: This should be determined by e.g. an Igorfile and not hard-coded.
     */
    private static final String BASE_PACKAGE = "com.arassec.igor";

    /**
     * Name of the annotation class' label() method.
     */
    private static final String LABEL_METHOD_NAME = "label";

    /**
     * Contains the categories of models this factory provides.
     */
    protected Set<ModelDefinition> categories;

    /**
     * Contains the types of models this factory provides.
     */
    protected Set<ModelDefinition> types;

    /**
     * Contains the categories and their corresponding types.
     */
    private Map<String, Set<ModelDefinition>> typesByCategory = new HashMap<>();

    /**
     * Contains the category for every type.
     */
    private Map<String, ModelDefinition> categoryByType = new HashMap<>();

    /**
     * Creates a new {@link ModelFactory}.
     *
     * @param modelClass              The model class this factory provides, e.g. 'Service'.
     * @param categoryAnnotationClass The annotation that defines categories for the models of this factory, e.g. 'IgorServiceCategory'.
     * @param typeAnnotationClass     The annotation that marks a class as type for this factory, e.g. 'IgorService'.
     */
    public ModelFactory(Class<T> modelClass, Class<? extends Annotation> categoryAnnotationClass, Class<? extends Annotation> typeAnnotationClass) {
        log.info("Registering categories using: {}", categoryAnnotationClass.getName());
        categories = findModelDefinitions(modelClass, categoryAnnotationClass);

        log.info("Registering types using: {}", typeAnnotationClass.getName());
        types = findModelDefinitions(modelClass, typeAnnotationClass);

        types.stream().forEach(type -> {
            ModelDefinition modelDefinition = findModelDefintion(modelClass, categories);
            if (!typesByCategory.containsKey(modelDefinition.getType())) {
                typesByCategory.put(modelDefinition.getType(), new HashSet<>());
            }
            typesByCategory.get(modelDefinition.getType()).add(type);
            categoryByType.put(type.getType(), modelDefinition);
        });
    }

    /**
     * Creates a new model instance of the provided type.
     *
     * @param type The model type to create.
     * @return An instance of the model or null, if none could be created.
     */
    public T createInstance(String type) {
        if (types.contains(type)) {
            try {
                return (T) Class.forName(type).getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        log.error("Unknown type: " + type + ".");
        return null;
    }

    /**
     * Creates a new model instance of the provided type and sets the parameters to the new instance.
     *
     * @param type       The model type to create.
     * @param parameters The model's parameters, that should be applied to the model.
     * @return The model instance with cleartext properties
     */
    public T createInstance(String type, Map<String, Object> parameters) {
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
    public ModelDefinition getCategory(T instance) {
        String type = getType(instance).getType();
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
    public ModelDefinition getType(T instance) {
        return types.stream().filter(modelDefinition -> modelDefinition.getType().equals(instance.getClass().getName()))
                .findFirst().orElse(null);
    }

    /**
     * Finds the model definition for the supplied class in the set of supplied model definitions.
     *
     * @param modelClass       The model class to find a definition for.
     * @param modelDefinitions The available model definitions.
     * @return A model definition for the supplied class.
     */
    private ModelDefinition findModelDefintion(Class<T> modelClass, Set<ModelDefinition> modelDefinitions) {
        for (ModelDefinition modelDefinition : modelDefinitions) {
            try {
                if (Class.forName(modelDefinition.getType()).isAssignableFrom(modelClass)) {
                    return modelDefinition;
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        // throw new IllegalArgumentException("No model definition for class '" + modelClass.getName() + "' defined!");
        return new ModelDefinition();
    }

    /**
     * Registers model definitions for the provided model and annotation class.
     *
     * @param modelClass      The model class this factory provides.
     * @param annotationClass The annotation that defines the model definition, e.g. a type or category annotation.
     * @return Set of {@link ModelDefinition}s.
     */
    private Set<ModelDefinition> findModelDefinitions(Class<T> modelClass, Class<? extends Annotation> annotationClass) {
        Set<ModelDefinition> result = new HashSet<>();

        if (modelClass != null && annotationClass != null) {
            ClassPathScanningCandidateComponentProvider scanner =
                    new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(annotationClass));

            for (BeanDefinition beanDefinition : scanner.findCandidateComponents(BASE_PACKAGE)) {
                try {
                    Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());
                    if (modelClass.isAssignableFrom(clazz)) {
                        ModelDefinition modelDefinition = new ModelDefinition();
                        modelDefinition.setType(clazz.getName());
                        modelDefinition.setLabel(String.valueOf(annotationClass.getMethod(LABEL_METHOD_NAME).invoke(clazz.getAnnotation(annotationClass))));
                        log.debug("Registered {} / '{}'", modelDefinition.getType(), modelDefinition.getLabel());
                        result.add(modelDefinition);
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
    public Set<ModelDefinition> getCategories() {
        return categories;
    }

    /**
     * Returns the type definitions this factory provides.
     *
     * @return Set of type definitions.
     */
    public Set<ModelDefinition> getTypes() {
        return types;
    }

    /**
     * Returns the categories and their corresponding types.
     *
     * @return categories and their types.
     */
    public Map<String, Set<ModelDefinition>> getTypesByCategory() {
        return typesByCategory;
    }

    /**
     * Returns the category for every type.
     */
    public Map<String, ModelDefinition> getCategoryByType() {
        return categoryByType;
    }

}
