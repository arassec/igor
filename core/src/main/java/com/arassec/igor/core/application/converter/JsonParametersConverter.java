package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.application.converter.util.EncryptionUtil;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.service.ServiceException;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Converts parameters from and to their JSON representation.
 */
@Slf4j
@Component("jsonParametersConverter")
@RequiredArgsConstructor
public class JsonParametersConverter {

    /**
     * Utlity for encryption/decryption.
     */
    private final EncryptionUtil encryptionUtil;

    /**
     * Returns the instance's paremters as {@link JSONArray}.
     *
     * @param instance The model instance to get the parameters from.
     * @param config   The converter configuration.
     * @param <T>      The instance type.
     *
     * @return Map containing the parameters.
     */
    public <T> JSONArray convert(T instance, ConverterConfig config) {

        JSONArray result = new JSONArray();
        List<JSONObject> parameterList = new LinkedList<>();
        Set<String> fixedFields = new HashSet<>();
        ReflectionUtils.doWithFields(instance.getClass(), field -> {
            if (field.isAnnotationPresent(IgorParam.class)) {
                if (field.getAnnotation(IgorParam.class).configurable()) {
                    JSONObject parameter = new JSONObject();
                    parameter.put(JsonKeys.NAME, field.getName());
                    Object value = getFieldValue(instance, field, config);
                    parameter.put(JsonKeys.VALUE, value);
                    parameter.put(JsonKeys.SECURED, field.getAnnotation(IgorParam.class).secured());
                    if (config.isAnnotationDataEnabled()) {
                        parameter.put(JsonKeys.TYPE, field.getType().getName());
                        parameter.put(JsonKeys.OPTIONAL, field.getAnnotation(IgorParam.class).optional());
                        parameter.put(JsonKeys.SUBTYPE, field.getAnnotation(IgorParam.class).subtype().name());
                        parameter.put(JsonKeys.CONFIGURABLE, true);
                    }
                    boolean isService = Service.class.isAssignableFrom(field.getType());
                    parameter.put(JsonKeys.SERVICE, isService);
                    if (isService && value != null) {
                        Service service = (Service) value;
                        if (config.isAnnotationDataEnabled()) {
                            parameter.put(JsonKeys.SERVICE_NAME, service.getName());
                        }
                        parameter.put(JsonKeys.VALUE, service.getId());
                    }
                    parameterList.add(parameter);
                } else {
                    fixedFields.add(field.getName());
                }
            }
        });

        parameterList.forEach(parameter -> {
            if (fixedFields.contains(parameter.getString(JsonKeys.NAME))) {
                parameter.put(JsonKeys.CONFIGURABLE, false);
            }
        });

        parameterList.sort((o1, o2) -> {
            boolean firstOptional = o1.optBoolean(JsonKeys.OPTIONAL);
            boolean secondOptional = o2.optBoolean(JsonKeys.OPTIONAL);
            if (firstOptional && !secondOptional) {
                return 1;
            } else if (!firstOptional && secondOptional) {
                return -1;
            }
            return 0;
        });

        parameterList.forEach(result::put);

        return result;
    }

    /**
     * Returns the provided parameters as Map.
     *
     * @param parameters    The parameters in JSON form.
     * @param config     The converter configuration.
     *
     * @return A map containing only parameter name and parameter value.
     */
    public Map<String, Object> convert(JSONArray parameters, ConverterConfig config) {
        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < parameters.length(); i++) {
            convertParameter(parameters.getJSONObject(i), result, config);
        }
        return result;
    }

    /**
     * Converts a single parameter from JSON to Map.
     *
     * @param parameter The parameter in JSON form.
     * @param target    The target map where the converted parameter should be stored in.
     * @param config    The converter configuration.
     */
    protected void convertParameter(JSONObject parameter, Map<String, Object> target, ConverterConfig config) {
        Object value = parameter.opt(JsonKeys.VALUE);
        if (value != null) {
            if (value instanceof String && StringUtils.isEmpty(value)) {
                return;
            } else if (parameter.getBoolean(JsonKeys.SECURED) && config.isSecurityEnabled()) {
                target.put(parameter.getString(JsonKeys.NAME), encryptionUtil.decrypt((String) value));
            } else {
                target.put(parameter.getString(JsonKeys.NAME), value);
            }
        }
    }

    /**
     * Returns the value of the provided instance's property.
     *
     * @param instance The instance of a class to get the field's value from.
     * @param field    The property of the instance to get the value from.
     * @param config   The converter configuration.
     * @param <T>      The instance type.
     *
     * @return The propertie's value.
     */
    private <T> Object getFieldValue(T instance, Field field, ConverterConfig config) {
        if (field != null) {
            try {
                Object result;
                field.setAccessible(true);
                if (isSecured(field) && config.isSecurityEnabled()) {
                    result = encryptionUtil.encrypt((String) field.get(instance));
                } else {
                    result = field.get(instance);
                }
                field.setAccessible(false);
                return result;
            } catch (IllegalAccessException e) {
                throw new ServiceException("Could not read property value!", e);
            }
        }
        return null;
    }

    /**
     * Indicates whether a property is secured or not.
     *
     * @param field The property to check.
     *
     * @return {@code true}, if the property is secured, {@code false} otherwise.
     */
    private boolean isSecured(Field field) {
        IgorParam igorParam = field.getAnnotation(IgorParam.class);
        return igorParam.secured() && field.getType().isAssignableFrom(String.class);
    }

}
