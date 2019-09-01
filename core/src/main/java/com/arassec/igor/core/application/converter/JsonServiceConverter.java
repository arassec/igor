package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.application.factory.ServiceFactory;
import com.arassec.igor.core.model.service.Service;
import com.github.openjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Converts {@link Service}s into their JSON representation and vice versa.
 */
@Slf4j
@Component
public class JsonServiceConverter extends JsonBaseConverter {

    /**
     * Factory for services.
     */
    private final ServiceFactory serviceFactory;

    /**
     * Converter for parameters.
     */
    private final JsonParametersConverter parametersConverter;

    /**
     * Creates a new converter instance.
     *
     * @param serviceFactory      The factory for {@link Service}s.
     * @param parametersConverter The converter for parameters.
     */
    public JsonServiceConverter(ServiceFactory serviceFactory,
                                @Qualifier("jsonParametersConverter") JsonParametersConverter parametersConverter) {
        this.serviceFactory = serviceFactory;
        this.parametersConverter = parametersConverter;
    }

    /**
     * Converts a {@link Service} into a JSON-string.
     *
     * @param service The service to convert.
     * @param config  The converter configuration.
     *
     * @return The service as JSON-string.
     */
    public JSONObject convert(Service service, ConverterConfig config) {
        JSONObject serviceJson = new JSONObject();
        serviceJson.put(JsonKeys.ID, service.getId());
        serviceJson.put(JsonKeys.NAME, service.getName());
        serviceJson.put(JsonKeys.CATEGORY, convertKeyLabelStore(serviceFactory.getCategory(service)));
        serviceJson.put(JsonKeys.TYPE, convertKeyLabelStore(serviceFactory.getType(service)));
        serviceJson.put(JsonKeys.PARAMETERS, parametersConverter.convert(service, config));
        return serviceJson;
    }

    /**
     * Converts the provided JSON-string into a {@link Service} instance.
     *
     * @param serviceJson The service in JSON form.
     * @param id          Optional ID of the service. Used, if the service's JSON doesn't contain an ID.
     * @param config      The converter configuration.
     *
     * @return A newly created service instance.
     */
    public Service convert(JSONObject serviceJson, Long id, ConverterConfig config) {
        String name = serviceJson.getString(JsonKeys.NAME);
        String type = convertKeyLabelStore(serviceJson.getJSONObject(JsonKeys.TYPE)).getKey();
        Map<String, Object> parameters = parametersConverter
                .convert(serviceJson.optJSONArray(JsonKeys.PARAMETERS), config);
        Service result = serviceFactory.createInstance(type, parameters);
        if (result != null) {
            result.setId(serviceJson.optLong(JsonKeys.ID, -1));
            if (result.getId() == -1) {
                if (id != null) {
                    result.setId(id);
                } else {
                    result.setId(null);
                }
            }
            result.setName(name);
        }
        return result;
    }

}
