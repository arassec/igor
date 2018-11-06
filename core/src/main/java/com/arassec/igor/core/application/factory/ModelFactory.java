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
 *
 */
@Slf4j
public abstract class ModelFactory<T> implements InitializingBean {

    protected Set<String> types = new HashSet<>();

    @Value("${igor.security.parameters.key}")
    private String password;

    /**
     * TODO: With Java-9 this should be changed to strong encryption!
     */
    private StrongTextEncryptor textEncryptor = new StrongTextEncryptor();

    @Override
    public void afterPropertiesSet() {
        textEncryptor.setPassword(password);
    }

    public ModelFactory(Class<T> modelClass, Class<? extends Annotation> annotationClass) {
        Security.setProperty("crypto.policy", "unlimited");
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(annotationClass));
        // TODO: Get this from "Igorfile"...
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

    public T createInstance(String type) {
        if (types.contains(type)) {
            try {
                return (T) Class.forName(type).getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        log.error("Unknown type: " + type + ". Job won't run!");
        return null;
    }

    public T createInstance(String type, Map<String, Object> parameters) {
        return createInstance(type, parameters, true);
    }

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

    public Map<String, Object> getParameters(T instance) {
        return getParameters(instance, false);
    }

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

    private boolean isSecured(Field field) {
        Annotation annotation = field.getAnnotation(IgorParam.class);
        IgorParam igorParam = (IgorParam) annotation;
        return igorParam.secured() && field.getType().isAssignableFrom(String.class);
    }

    public Set<String> getTypes() {
        return types;
    }
}
