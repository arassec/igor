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
 *
 */
@Component
public class ServiceConverter {

    @Autowired
    private ServiceFactory serviceFactory;

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
