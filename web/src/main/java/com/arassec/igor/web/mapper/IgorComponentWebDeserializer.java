package com.arassec.igor.web.mapper;

import com.arassec.igor.application.registry.IgorComponentRegistry;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.repository.ConnectorRepository;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deserializer for igor components in the web layer.
 */
public abstract class IgorComponentWebDeserializer<T extends IgorComponent> extends StdDeserializer<T> {

    /**
     * The igor component registry.
     */
    final transient IgorComponentRegistry igorComponentRegistry;

    /**
     * The {@link ConnectorRepository} to load connectors as parameter values.
     */
    private final transient ConnectorRepository connectorRepository;

    /**
     * Creates a new deserializer instance.
     *
     * @param connectorRepository The connector repository to load connectors from.
     */
    IgorComponentWebDeserializer(Class<T> clazz, IgorComponentRegistry igorComponentRegistry,
                                 ConnectorRepository connectorRepository) {
        super(clazz);
        this.igorComponentRegistry = igorComponentRegistry;
        this.connectorRepository = connectorRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        @SuppressWarnings("unchecked")
        Map<String, Object> map = deserializationContext.readValue(jsonParser, Map.class);

        @SuppressWarnings("unchecked")
        String typeId = getTypeId((Map<String, Object>) map.get(WebMapperKey.TYPE.getKey()));

        @SuppressWarnings("unchecked")
        Map<String, Object> parameters =
                deserializeParameters((List<Map<String, Object>>) map.get(WebMapperKey.PARAMETERS.getKey()));

        T instance = createInstance(typeId, parameters);

        if (instance == null) {
            throw new IllegalStateException("Could not create instance for type-id: " + typeId);
        }

        setComponentSpecifica(instance, map);

        return instance;
    }

    /**
     * Creates an instance of the igor component.
     * <p>
     * Has to be implemented by subclasses to provide an actual instance of a concrete component type (e.g. Action).
     *
     * @param typeId     The component's type ID.
     * @param parameters The component's parameters.
     *
     * @return A newly created component instance.
     */
    abstract T createInstance(String typeId, Map<String, Object> parameters);

    /**
     * Deserializes the JSON parameters.
     *
     * @param parameters The parameters in JSON form.
     *
     * @return The parameters as Map of objects.
     */
    private Map<String, Object> deserializeParameters(List<Map<String, Object>> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return null;
        }
        Map<String, Object> result = new HashMap<>();
        parameters.forEach(jsonParameter -> {
            String parameterName = String.valueOf(jsonParameter.get(WebMapperKey.NAME.getKey()));
            if (jsonParameter.containsKey(WebMapperKey.CONNECTOR.getKey()) && (boolean) jsonParameter.get(WebMapperKey.CONNECTOR.getKey()) && connectorRepository != null) {
                result.put(parameterName, deserializeConnectorParameter(jsonParameter));
            } else {
                Object value = jsonParameter.get(WebMapperKey.VALUE.getKey());
                if (parameterValueValid(value)) {
                    result.put(parameterName, value);
                }
            }
        });
        return result;
    }

    /**
     * Checks if a parameter value is valid for further processing.
     *
     * @param value The value to check.
     *
     * @return {@code true} if the parameter's value is valid and should be further processed, {@code false} otherwise.
     */
    private boolean parameterValueValid(Object value) {
        if (value == null) {
            return false;
        }
        return !(value instanceof String) || StringUtils.hasText((String) value);
    }

    /**
     * Deserializes a connector parameter.
     *
     * @param jsonParameter The parameter in JSON form.
     *
     * @return The newly created connector instance.
     */
    private Object deserializeConnectorParameter(Map<String, Object> jsonParameter) {
        return connectorRepository.findById(String.valueOf(jsonParameter.get(WebMapperKey.VALUE.getKey())));
    }

    /**
     * Returns the type of the component.
     *
     * @param map The component as map.
     *
     * @return The type of the component.
     */
    private String getTypeId(Map<String, Object> map) {
        Object type = map.get(WebMapperKey.KEY.getKey());
        if (type instanceof String) {
            return (String) type;
        }
        return null;
    }

    /**
     * Sets component specific properties using the provided raw data.
     *
     * @param instance      The newly created component instance.
     * @param componentJson The component data in JSON form.
     */
    protected void setComponentSpecifica(IgorComponent instance, Map<String, Object> componentJson) {
        if (componentJson.containsKey(WebMapperKey.ID.getKey())) {
            instance.setId(String.valueOf(componentJson.get(WebMapperKey.ID.getKey())));
        }
    }

}
