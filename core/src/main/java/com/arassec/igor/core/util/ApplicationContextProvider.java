package com.arassec.igor.core.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

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
        this.context = context;
    }

    /**
     * Returns a bean from the spring context of the specified class.
     *
     * @param aClass Class of the bean.
     * @param <T>    The type of the bean.
     * @return The bean.
     */
    public static <T> T getBean(Class<T> aClass) {
        return context.getBean(aClass);
    }

}
