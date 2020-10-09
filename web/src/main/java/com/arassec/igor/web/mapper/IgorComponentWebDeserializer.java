package com.arassec.igor.web.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.repository.ConnectorRepository;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.web.simulation.ConnectorProxy;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
     * Can be set to {@code true} to deserialize components in simulation mode, i.e. wrap proxies around them for job
     * simulations.
     */
    private final boolean simulationMode;

    /**
     * Creates a new deserializer instance.
     *
     * @param connectorRepository The connector repository to load connectors from.
     * @param simulationMode      Set to {@code true} if the resulting  components are used during simulated job runs.
     */
    IgorComponentWebDeserializer(Class<T> clazz, IgorComponentRegistry igorComponentRegistry,
                                 ConnectorRepository connectorRepository,
                                 boolean simulationMode) {
        super(clazz);
        this.igorComponentRegistry = igorComponentRegistry;
        this.connectorRepository = connectorRepository;
        this.simulationMode = simulationMode;
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

        T instance = createInstance(typeId, parameters, simulationMode);

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
    abstract T createInstance(String typeId, Map<String, Object> parameters, boolean simulationMode);

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
        return !(value instanceof String) || !StringUtils.isEmpty(value);
    }

    /**
     * Deserializes a connector parameter.
     *
     * @param jsonParameter The parameter in JSON form.
     *
     * @return The newly created connector instance.
     */
    private Object deserializeConnectorParameter(Map<String, Object> jsonParameter) {
        if (simulationMode) {
            String connectorId = String.valueOf(jsonParameter.get(WebMapperKey.VALUE.getKey()));
            Connector connector = connectorRepository.findById(connectorId);
            if (connector == null) {
                throw new IllegalArgumentException("No connector with ID " + connectorId + " found!");
            }
            try {
                return new ByteBuddy()
                        .subclass(connector.getClass())
                        .method(ElementMatchers.any())
                        .intercept(InvocationHandlerAdapter.of(new ConnectorProxy(connector)))
                        .make()
                        .load(connector.getClass().getClassLoader())
                        .getLoaded()
                        .getDeclaredConstructor()
                        .newInstance();
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IgorException("Could not create connector proxy!", e);
            }
        } else {
            return connectorRepository.findById(String.valueOf(jsonParameter.get(WebMapperKey.VALUE.getKey())));
        }
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
