package com.arassec.igor.core.application.factory;

import com.arassec.igor.core.model.IgorParam;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.text.StrongTextEncryptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.Security;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base class for Factories.
 */
@Slf4j
public abstract class ModelFactory<T> implements InitializingBean {

    /**
     * Contains the types of models this factory provides.
     */
    protected Set<String> types = new HashSet<>();

    /**
     * Password for property encryption.
     */
    @Value("${igor.security.parameters.key}")
    private String password;

    /**
     * Provides encryption for secured properties.
     */
    private StrongTextEncryptor textEncryptor = new StrongTextEncryptor();

    /**
     * Prepares the property encryption.
     */
    @Override
    public void afterPropertiesSet() {
        textEncryptor.setPassword(password);
    }

    /**
     * Creates a new {@link ModelFactory}.
     *
     * @param modelClass      The model class this factory provides.
     * @param annotationClass The annotation that marks a class as model for this factory.
     */
    public ModelFactory(Class<T> modelClass, Class<? extends Annotation> annotationClass) {
        Security.setProperty("crypto.policy", "unlimited");
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(annotationClass));
        // TODO: Get this from e.g. "Igorfile" or something else... Anything that doesn't scan the whole classpath.
        for (BeanDefinition beanDefinition : scanner.findCandidateComponents("com.arassec.igor")) {
            try {
                Class<?> c = Class.forName(beanDefinition.getBeanClassName());
                if (modelClass.isAssignableFrom(c)) {
                    String className = beanDefinition.getBeanClassName();
                    types.add(className);
                    log.debug("Registered as {}: {}", annotationClass.getName(), beanDefinition.getBeanClassName());
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
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
     * @return The model instance with set parameters.
     */
    public T createInstance(String type, Map<String, Object> parameters) {
        return createInstance(type, parameters, true);
    }

    /**
     * Creates a new model instance of the provided type and sets the parameters to the new instance.
     *
     * @param type                    The model type to create.
     * @param parameters              The model's parameters, that should be applied to the model.
     * @param encryptedValuesProvided Indicates whether secured properties are already encrypted (when set to
     *                                {@code true}) or in cleartext form (when set to {@code false}).
     * @return The model instance with cleartext properties
     */
    public T createInstance(String type, Map<String, Object> parameters, boolean encryptedValuesProvided) {
        T instance = createInstance(type);
        if (instance == null) {
            return null;
        }
        if (parameters != null && !parameters.isEmpty()) {
            ReflectionUtils.doWithFields(instance.getClass(), field -> {
                if (field.isAnnotationPresent(IgorParam.class) && parameters.containsKey(field.getName())) {
                    try {
                        field.setAccessible(true);
                        if (isSecured(field) && encryptedValuesProvided) {
                            field.set(instance, textEncryptor.decrypt((String) parameters.get(field.getName())));
                        } else if (isSecured(field) && !encryptedValuesProvided) {
                            // The field's value is not secured at the moment, so it doesn't need to be decrypted!
                            field.set(instance, parameters.get(field.getName()));
                        } else {
                            field.set(instance, parameters.get(field.getName()));
                        }
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
     * Returns the instances paremters.
     *
     * @param instance The model instance to get the parameters from.
     * @return Map containing the parameters. May contain encrypted parameter values for secured parameters.
     */
    public Map<String, Object> getParameters(T instance) {
        return getParameters(instance, false);
    }

    /**
     * Returns the instances paremters.
     *
     * @param instance            The model instance to get the parameters from.
     * @param keepClearTextValues Set to {@code true} to keep secured properties in cleartext form. If set to
     *                            {@code false}, secured properties will be encrypted.
     * @return Map containing the parameters.
     */
    public Map<String, Object> getParameters(T instance, boolean keepClearTextValues) {
        Map<String, Object> parameters = new HashMap<>();
        ReflectionUtils.doWithFields(instance.getClass(), field -> {
            if (field.isAnnotationPresent(IgorParam.class)) {
                try {
                    field.setAccessible(true);
                    if (isSecured(field) && !keepClearTextValues) {
                        parameters.put(field.getName(), textEncryptor.encrypt((String) field.get(instance)));
                    } else {
                        parameters.put(field.getName(), field.get(instance));
                    }
                    field.setAccessible(false);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        });
        return parameters;
    }

    /**
     * Indicates whether a property is secured or not.
     *
     * @param field The property to check.
     * @return {@code true}, if the property is secured, {@code false} otherwise.
     */
    private boolean isSecured(Field field) {
        Annotation annotation = field.getAnnotation(IgorParam.class);
        IgorParam igorParam = (IgorParam) annotation;
        return igorParam.secured() && field.getType().isAssignableFrom(String.class);
    }

    /**
     * Returns the model types this factory provides.
     *
     * @return Set of model types.
     */
    public Set<String> getTypes() {
        return types;
    }
}
