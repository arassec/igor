package com.arassec.igor.core.application.factory;

import com.arassec.igor.core.model.IgorParam;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public abstract class ModelFactory<T> {

    private static final Logger LOG = LoggerFactory.getLogger(ModelFactory.class);

    protected Map<String, String> typeToClass = new HashMap<>();

    @Value("${igor.security.parameters.key}")
    private String password;

    /**
     * TODO: With Java-9 this should be changed to strong encryption!
     */
    private BasicTextEncryptor textEncryptor = new BasicTextEncryptor();

    @PostConstruct
    public void initialize() {
        textEncryptor.setPassword(password);
    }

    public ModelFactory() {
        // TODO: Java-9 Feature: no more JCE-Downloads?!?
        Security.setProperty("crypto.policy", "unlimited");
    }

    public T createInstance(String type) {
        if (typeToClass.containsKey(type)) {
            try {
                return (T) Class.forName(typeToClass.get(type)).newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        LOG.error("Unknown type: " + type + ". Job won't run!");
        return null;
    }

    public T createInstance(String type, Map<String, Object> parameters) {
        return createInstance(type, parameters, true);
    }

    public T createInstance(String type, Map<String, Object> parameters, boolean secured) {
        T instance = createInstance(type);
        if (instance == null) {
            return null;
        }
        ReflectionUtils.doWithFields(instance.getClass(), field -> {
            if (field.isAnnotationPresent(IgorParam.class) && parameters.containsKey(field.getName())) {
                try {
                    field.setAccessible(true);
                    if (isSecured(field) && secured) {
                        field.set(instance, textEncryptor.decrypt((String) parameters.get(field.getName())));
                    } else if (isSecured(field) && !secured) {
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
        return instance;
    }

    public Map<String, Object> getParameters(T instance) {
        Map<String, Object> parameters = new HashMap<>();
        ReflectionUtils.doWithFields(instance.getClass(), field -> {
            if (field.isAnnotationPresent(IgorParam.class)) {
                try {
                    field.setAccessible(true);
                    if (isSecured(field)) {
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

}
