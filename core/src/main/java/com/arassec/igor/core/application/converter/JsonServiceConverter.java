package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.application.factory.ServiceFactory;
import com.arassec.igor.core.model.service.Service;
import com.github.openjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Converts {@link Service}s into their JSON representation and vice versa.
 */
@Slf4j
@Component
public class JsonServiceConverter extends JsonBaseConverter {

    /**
     * Converter for parameters.
     */
    @Autowired
    private JsonParametersConverter parameterConverter;

    /**
     * Factory for services.
     */
    @Autowired
    private ServiceFactory serviceFactory;

    /**
     * Converts a {@link Service} into a JSON-string.
     *
     * @param service The service to convert.
     * @param applySecurity Set to {@code true} to decrypt secured properties.
     * @param addVolatile   Set to {@code true} to add properties that only exist through annotations or could otherwise
     *                      be obtained, but can be added for convenience.
     * @return The service as JSON-string.
     */
    public JSONObject convert(Service service, boolean applySecurity, boolean addVolatile) {
        JSONObject serviceJson = new JSONObject();
        serviceJson.put(JsonKeys.ID, service.getId());
        serviceJson.put(JsonKeys.NAME, service.getName());
        serviceJson.put(JsonKeys.CATEGORY, convert(serviceFactory.getCategory(service)));
        serviceJson.put(JsonKeys.TYPE, convert(serviceFactory.getType(service)));
        serviceJson.put(JsonKeys.PARAMETERS, parameterConverter.convert(service, applySecurity, addVolatile));
        return serviceJson;
    }

    /**
     * Converts the provided JSON-string into a {@link Service} instance.
     *
     * @param serviceJson The service in JSON form.
     * @return A newly created service instance.
     */
    public Service convert(JSONObject serviceJson, boolean applySecurity) {
        String name = serviceJson.getString(JsonKeys.NAME);
        String type = convert(serviceJson.getJSONObject(JsonKeys.TYPE)).getKey();
        Map<String, Object> parameters = parameterConverter.convert(serviceJson.optJSONArray(JsonKeys.PARAMETERS), applySecurity);
        Service result = serviceFactory.createInstance(type, parameters);
        if (result != null) {
            result.setId(serviceJson.optLong(JsonKeys.ID, -1));
            if (result.getId() == -1) {
                result.setId(null);
            }
            result.setName(name);
        }
        return result;
    }

}
