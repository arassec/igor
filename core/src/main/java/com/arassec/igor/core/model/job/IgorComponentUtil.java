package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.service.Service;
import org.springframework.util.ReflectionUtils;

/**
 * Utility class for the work with {@link com.arassec.igor.core.model.IgorComponent}s.
 */
class IgorComponentUtil {

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
        ReflectionUtils.doWithFields(igorComponent.getClass(), field -> {
            if (field.isAnnotationPresent(IgorParam.class)) {
                if (Service.class.isAssignableFrom(field.getType())) {
                    try {
                        boolean accessibility = field.canAccess(igorComponent);
                        field.setAccessible(true);
                        Object value = field.get(igorComponent);
                        field.setAccessible(accessibility);
                        if (value != null) {
                            ((Service) value).initialize(jobId, taskId, jobExecution);
                        }
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Could not initialize service!", e);
                    }
                }
            }
        });
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
        ReflectionUtils.doWithFields(igorComponent.getClass(), field -> {
            if (field.isAnnotationPresent(IgorParam.class)) {
                if (Service.class.isAssignableFrom(field.getType())) {
                    try {
                        boolean accessibility = field.canAccess(igorComponent);
                        field.setAccessible(true);
                        Object value = field.get(igorComponent);
                        field.setAccessible(accessibility);
                        if (value != null) {
                            ((Service) value).shutdown(jobId, taskId, jobExecution);
                        }
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Could not initialize service!", e);
                    }
                }
            }
        });
    }

}
