package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.job.execution.JobExecution;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Utility class for the work with {@link com.arassec.igor.core.model.IgorComponent}s.
 */
public class IgorComponentUtil {

    /**
     * Prevents instantiation.
     */
    private IgorComponentUtil() {
    }

    /**
     * Initializes {@link Connector}s used by the supplied {@link IgorComponent}.
     *
     * @param igorComponent The component.
     * @param jobExecution  Contains information about the job execution.
     */
    public static void initializeConnectors(IgorComponent igorComponent, JobExecution jobExecution) {
        if (igorComponent == null) {
            return;
        }
        Class<?> current = igorComponent.getClass();
        do {
            Arrays.stream(current.getDeclaredFields()).forEach(field -> processField(igorComponent, jobExecution, field,
                    true));
            current = current.getSuperclass();
        } while (current != null && !current.equals(Object.class));
    }

    /**
     * Shuts {@link Connector}s used by the supplied {@link IgorComponent} down.
     *
     * @param igorComponent The component.
     * @param jobExecution  Contains information about the job execution.
     */
    public static void shutdownConnectors(IgorComponent igorComponent, JobExecution jobExecution) {
        if (igorComponent == null) {
            return;
        }
        Class<?> current = igorComponent.getClass();
        do {
            Arrays.stream(current.getDeclaredFields()).forEach(field -> processField(igorComponent, jobExecution, field,
                    false));
            current = current.getSuperclass();
        } while (current != null && !current.equals(Object.class));
    }

    /**
     * Initializes or shuts down a connector if the supplied field contains one.
     *
     * @param igorComponent The component.
     * @param jobExecution  Contains information about the job execution.
     * @param field         The field possibly containing a connector.
     * @param initialize    Set to {@code true} to initialize the connector or to {@code false} to shut it down.
     */
    private static void processField(IgorComponent igorComponent, JobExecution jobExecution,
                                     Field field, boolean initialize) {
        if (field.isAnnotationPresent(IgorParam.class)) {
            try {
                if ((!Modifier.isPublic(field.getModifiers()) ||
                        !Modifier.isPublic(field.getDeclaringClass().getModifiers()) ||
                        Modifier.isFinal(field.getModifiers())) && !field.canAccess(igorComponent)) {
                    field.setAccessible(true); //NOSONAR - @IgorParam-fields must not necessarily be accessible!
                }
                Object value = field.get(igorComponent);
                if (value instanceof Connector) {
                    if (initialize) {
                        ((Connector) value).initialize(jobExecution);
                    } else {
                        ((Connector) value).shutdown(jobExecution);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Could not initialize connector!", e);
            }
        }
    }

}
