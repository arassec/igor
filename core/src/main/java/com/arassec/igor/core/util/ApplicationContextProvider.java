package com.arassec.igor.core.util;

import com.arassec.igor.core.model.IgorComponent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Provides the Spring-Boot application context to non-spring beans.
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    /**
     * The application context.
     */
    private static ApplicationContext context;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setApplicationContext(ApplicationContext context) {
        ApplicationContextProvider.context = context;
    }

    /**
     * Returns a bean from the spring context of the specified class.
     *
     * @param aClass Class of the bean.
     * @param <T>    The type of the bean.
     *
     * @return The bean.
     */
    public static <T> T getBean(Class<T> aClass) {
        return context.getBean(aClass);
    }

    /**
     * Returns an instance of a igor component with the specified class and type ID.
     *
     * @param aClass The class.
     * @param typeId The type ID of the igor component.
     * @param <T>    The type parameter.
     *
     * @return A newly created Instance.
     */
    public static <T extends IgorComponent> T getIgorComponent(Class<T> aClass, String typeId) {
        Map<String, T> beansOfType = context.getBeansOfType(aClass);
        for (T instance : beansOfType.values()) {
            if (instance.getTypeId().equals(typeId)) {
                return instance;
            }
        }
        throw new IllegalStateException("Invalid class or type ID: " + aClass + " / " + typeId);
    }

}
