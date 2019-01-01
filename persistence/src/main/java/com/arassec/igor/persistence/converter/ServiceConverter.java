package com.arassec.igor.persistence.converter;

import com.arassec.igor.core.application.factory.ServiceFactory;
import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.service.Service;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts {@link Service}s into their JSON representation and vice versa.
 */
@Component
public class ServiceConverter {

    /**
     * The service factory.
     */
    @Autowired
    private ServiceFactory serviceFactory;

    /**
     * Converts a {@link Service} into a JSON-string.
     *
     * @param service The service to convert.
     * @return The service as JSON-string.
     */
    public String convert(Service service) {
        JSONObject serviceJson = new JSONObject();
        serviceJson.put(JsonKeys.NAME, service.getName());
        serviceJson.put(JsonKeys.TYPE, service.getClass().getName());

        Map<String, Object> parameters = serviceFactory.getParameters(service);
        if (!parameters.isEmpty()) {
            JSONArray parameterArray = new JSONArray();
            for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
                if (parameter.getValue() != null) {
                    JSONObject param = new JSONObject();
                    param.put(parameter.getKey(), parameter.getValue());
                    parameterArray.put(param);
                }
            }
            serviceJson.put(JsonKeys.PARAMETERS, parameterArray);
        }

        return serviceJson.toString();
    }

    /**
     * Converts the provided JSON-string into a {@link Service} instance.
     *
     * @param serviceString The service JSON.
     * @return A newly created service instance.
     */
    public Service convert(String serviceString) {
        JSONObject jsonObject = new JSONObject(serviceString);
        String name = jsonObject.getString(JsonKeys.NAME);
        String type = jsonObject.getString(JsonKeys.TYPE);

        Map<String, Object> parameters = new HashMap<>();
        JSONArray parameterArray = jsonObject.optJSONArray(JsonKeys.PARAMETERS);
        if (parameterArray != null) {
            for (int i = 0; i < parameterArray.length(); i++) {
                JSONObject parameter = parameterArray.getJSONObject(i);
                if (parameter.names() != null) {
                    for (int j = 0; j < parameter.names().length(); j++) {
                        parameters.put(parameter.names().getString(j), parameter.get(parameter.names().getString(j)));
                    }
                }
            }
        }

        Service result = serviceFactory.createInstance(type, parameters);
        if (result != null) {
            result.setName(name);
        }

        return result;
    }

}
