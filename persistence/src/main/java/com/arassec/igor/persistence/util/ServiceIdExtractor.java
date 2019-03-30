package com.arassec.igor.persistence.util;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.service.Service;
import org.springframework.util.ReflectionUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Utility class to extract service IDs from igor model classes.
 */
public class ServiceIdExtractor {

    /**
     * Extracts service IDs from the parameters of the supplied class.
     *
     * @param instance The model instance.
     * @return List of referenced service IDs.
     */
    public static <T> List<Long> getServiceIds(T instance) {
        List<Long> result = new LinkedList<>();

        if (instance == null) {
            return result;
        }

        ReflectionUtils.doWithFields(instance.getClass(), field -> {
            if (field.isAnnotationPresent(IgorParam.class)) {
                boolean isService = Service.class.isAssignableFrom(field.getType());
                try {
                    field.setAccessible(true);
                    Object value = field.get(instance);
                    field.setAccessible(false);
                    if (isService && value != null) {
                        result.add(((Service) value).getId());
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Could not read service ID!", e);
                }
            }
        });

        return result;
    }

}
