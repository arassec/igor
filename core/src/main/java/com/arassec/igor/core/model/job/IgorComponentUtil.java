package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.service.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * Utility class for the work with {@link com.arassec.igor.core.model.IgorComponent}s.
 */
class IgorComponentUtil {

    /**
     * Prevents instantiation.
     */
    private IgorComponentUtil() {
    }

    /**
     * Initializes {@link Service}s used by the supplied {@link IgorComponent}.
     *
     * @param igorComponent The component.
     * @param jobId         The job's ID.
     * @param taskId        The task's ID.
     * @param jobExecution  Contains information about the job execution.
     */
    static void initializeServices(IgorComponent igorComponent, String jobId, String taskId, JobExecution jobExecution) {
        if (igorComponent == null) {
            return;
        }
        ReflectionUtils.doWithFields(igorComponent.getClass(), field -> processField(igorComponent, jobId, taskId, jobExecution, field, true));
    }

    /**
     * Shuts {@link Service}s used by the supplied {@link IgorComponent} down.
     *
     * @param igorComponent The component.
     * @param jobId         The job's ID.
     * @param taskId        The task's ID.
     * @param jobExecution  Contains information about the job execution.
     */
    static void shutdownServices(IgorComponent igorComponent, String jobId, String taskId, JobExecution jobExecution) {
        if (igorComponent == null) {
            return;
        }
        ReflectionUtils.doWithFields(igorComponent.getClass(), field -> processField(igorComponent, jobId, taskId, jobExecution, field, false));
    }

    /**
     * Initializes or shuts down a service if the supplied field contains one.
     *
     * @param igorComponent The component.
     * @param jobId         The job's ID.
     * @param taskId        The task's ID.
     * @param jobExecution  Contains information about the job execution.
     * @param field         The field possibly containing a service.
     * @param initialize    Set to {@code true} to initialize the service or to {@code false} to shut it down.
     */
    private static void processField(IgorComponent igorComponent, String jobId, String taskId, JobExecution jobExecution,
                                     Field field, boolean initialize) {
        if (field.isAnnotationPresent(IgorParam.class) && Service.class.isAssignableFrom(field.getType())) {
            try {
                ReflectionUtils.makeAccessible(field);
                Object value = field.get(igorComponent);
                if (value != null) {
                    if (initialize) {
                        ((Service) value).initialize(jobId, taskId, jobExecution);
                    } else {
                        ((Service) value).shutdown(jobId, taskId, jobExecution);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Could not initialize service!", e);
            }
        }
    }

}
