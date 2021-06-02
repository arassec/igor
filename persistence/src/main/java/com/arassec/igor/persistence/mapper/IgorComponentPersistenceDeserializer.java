package com.arassec.igor.persistence.mapper;

import com.arassec.igor.application.registry.IgorComponentRegistry;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.repository.ConnectorRepository;
import com.arassec.igor.persistence.security.SecurityProvider;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deserializer for igor components that converts JSON into components.
 */
@Slf4j
public abstract class IgorComponentPersistenceDeserializer<T extends IgorComponent> extends StdDeserializer<T> {

    /**
     * The component registry.
     */
    transient IgorComponentRegistry igorComponentRegistry;

    /**
     * Security provider for secured parameters that should be decrypted during deserialization.
     */
    private final transient SecurityProvider securityProvider;

    /**
     * The {@link ConnectorRepository} to load connectors as parameter values. Can be {@code null} if no connectors should be
     * loaded.
     */
    private final transient ConnectorRepository connectorRepository;

    /**
     * Creates a new deserializer.
     *
     * @param clazz                 The class parameter.
     * @param igorComponentRegistry The component registry.
     * @param connectorRepository   The repository for connectors. Can be {@code null} to ignore connectors as parameter values.
     * @param securityProvider      The security provider to decrypt secured parameter values.
     */
    IgorComponentPersistenceDeserializer(Class<T> clazz, IgorComponentRegistry igorComponentRegistry,
                                         ConnectorRepository connectorRepository, SecurityProvider securityProvider) {
        super(clazz);
        this.igorComponentRegistry = igorComponentRegistry;
        this.connectorRepository = connectorRepository;
        this.securityProvider = securityProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        @SuppressWarnings("unchecked")
        Map<String, Object> map = deserializationContext.readValue(jsonParser, Map.class);

        String typeId = getTypeId(map);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawParameters = (List<Map<String, Object>>) map.get(PersistenceMapperKey.PARAMETERS.getKey());

        Map<String, Object> parameters = deserializeParameters(rawParameters, typeId);

        var instance = createInstance(typeId, parameters);

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
     * @param typeId     The component's type ID.
     *
     * @return The parameters as Map of objects.
     */
    private Map<String, Object> deserializeParameters(List<Map<String, Object>> parameters, String typeId) {
        if (parameters == null || parameters.isEmpty()) {
            return null;
        }
        Map<String, Object> result = new HashMap<>();
        parameters.forEach(jsonParameter -> {
            var parameterName = String.valueOf(jsonParameter.get(PersistenceMapperKey.NAME.getKey()));
            if (jsonParameter.containsKey(PersistenceMapperKey.CONNECTOR.getKey()) && (boolean) jsonParameter.get(PersistenceMapperKey.CONNECTOR.getKey()) && connectorRepository != null) {
                result.put(parameterName,
                        connectorRepository.findById(String.valueOf(jsonParameter.get(PersistenceMapperKey.VALUE.getKey()))));
            } else if (jsonParameter.containsKey(PersistenceMapperKey.SECURED.getKey()) && (boolean) jsonParameter.get(PersistenceMapperKey.SECURED.getKey())) {
                result.put(parameterName, securityProvider.decrypt(typeId, parameterName,
                        String.valueOf(jsonParameter.get(PersistenceMapperKey.VALUE.getKey()))));
            } else {
                result.put(parameterName, jsonParameter.get(PersistenceMapperKey.VALUE.getKey()));
            }
        });
        return result;
    }

    /**
     * Returns the type of the component.
     *
     * @param map The component as map.
     *
     * @return The type of the component.
     */
    private String getTypeId(Map<String, Object> map) {
        Object type = map.get(PersistenceMapperKey.TYPE_ID.getKey());
        if (type instanceof String) {
            return (String) type;
        }
        return null;
    }

    /**
     * Sets component specific properties using the provided raw data.
     *
     * @param instance      The newly created component instance.
     * @param componentJson The component in JSON form.
     */
    protected void setComponentSpecifica(IgorComponent instance, Map<String, Object> componentJson) {
        if (componentJson.containsKey(PersistenceMapperKey.ID.getKey())) {
            instance.setId(String.valueOf(componentJson.get(PersistenceMapperKey.ID.getKey())));
        }
    }

}
