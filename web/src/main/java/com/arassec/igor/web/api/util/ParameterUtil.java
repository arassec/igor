package com.arassec.igor.web.api.util;

import com.arassec.igor.core.application.ServiceManager;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.web.api.model.ParameterDefinition;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class ParameterUtil {

    @Autowired
    private ServiceManager serviceManager;

    public <T> List<ParameterDefinition> getParameters(T instance) {
        List<ParameterDefinition> parameterDefinitions = new LinkedList<>();
        ReflectionUtils.doWithFields(instance.getClass(), field -> {
            if (field.isAnnotationPresent(IgorParam.class)) {
                String parameterName = field.getName();
                String parameterType = field.getType().getName();
                Object parameterValue = getFieldValue(instance, parameterName);
                boolean secured = field.getAnnotation(IgorParam.class).secured();
                boolean optional = field.getAnnotation(IgorParam.class).optional();
                boolean service = Service.class.isAssignableFrom(field.getType());
                parameterDefinitions.add(new ParameterDefinition(parameterName, parameterType, parameterValue, optional, secured, service));
            }
        });
        return parameterDefinitions;
    }

    public Map<String, Object> convertParameters(JSONArray parametersArray) {
        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < parametersArray.length(); i++) {
            JSONObject parameterObject = parametersArray.getJSONObject(i);
            Object value = parameterObject.opt("value");
            boolean isService = parameterObject.getBoolean("service");
            if (value != null) {
                if (value instanceof String && StringUtils.isEmpty(value)) {
                    continue;
                }
                if (isService && value instanceof Integer) {
                    result.put(parameterObject.getString("name"), serviceManager.load(Long.valueOf((Integer) value)));
                } else {
                    result.put(parameterObject.getString("name"), value);
                }
            }
        }
        return result;
    }

    private <T> Object getFieldValue(T instance, String parameterName) {
        Field valueField = ReflectionUtils.findField(instance.getClass(), parameterName);
        if (valueField != null) {
            try {
                valueField.setAccessible(true);
                Object value = valueField.get(instance);
                valueField.setAccessible(false);
                return value;
            } catch (IllegalAccessException e) {
                throw new ServiceException("Could not read property value!", e);
            }
        }
        return null;
    }

}
