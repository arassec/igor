package com.arassec.igor.web.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.web.simulation.ServiceProxy;
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
     * The {@link ServiceRepository} to load services as parameter values.
     */
    private final transient ServiceRepository serviceRepository;

    /**
     * Can be set to {@code true} to deserialize components in simulation mode, i.e. wrap proxies around them for job
     * simulations.
     */
    private final boolean simulationMode;

    /**
     * Creates a new deserializer instance.
     *
     * @param serviceRepository The service repository to load services from.
     * @param simulationMode    Set to {@code true} if the resulting  components are used during simulated job runs.
     */
    IgorComponentWebDeserializer(Class<T> clazz, IgorComponentRegistry igorComponentRegistry,
                                 ServiceRepository serviceRepository,
                                 boolean simulationMode) {
        super(clazz);
        this.igorComponentRegistry = igorComponentRegistry;
        this.serviceRepository = serviceRepository;
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
        Map<String, Object> parameters = deserializeParameters((List<Map<String, Object>>) map.get(WebMapperKey.PARAMETERS.getKey()));

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
            if (jsonParameter.containsKey(WebMapperKey.SERVICE.getKey()) && (boolean) jsonParameter.get(WebMapperKey.SERVICE.getKey()) && serviceRepository != null) {
                result.put(parameterName, deserializeServiceParameter(jsonParameter));
            } else {
                Object value = jsonParameter.get(WebMapperKey.VALUE.getKey());
                if (value != null) {
                    if (value instanceof String && StringUtils.isEmpty(value)) {
                        return;
                    }
                    result.put(parameterName, value);
                }
            }
        });
        return result;
    }

    /**
     * Deserializes a service parameter.
     *
     * @param jsonParameter The parameter in JSON form.
     *
     * @return The newly created service instance.
     */
    private Object deserializeServiceParameter(Map<String, Object> jsonParameter) {
        if (simulationMode) {
            String serviceId = String.valueOf(jsonParameter.get(WebMapperKey.VALUE.getKey()));
            Service service = serviceRepository.findById(serviceId);
            if (service == null) {
                throw new IllegalArgumentException("No service with ID " + serviceId + " found!");
            }
            try {
                return new ByteBuddy()
                        .subclass(service.getClass())
                        .method(ElementMatchers.any())
                        .intercept(InvocationHandlerAdapter.of(new ServiceProxy(service)))
                        .make()
                        .load(service.getClass().getClassLoader())
                        .getLoaded()
                        .getDeclaredConstructor()
                        .newInstance();
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IgorException("Could not create service proxy!", e);
            }
        } else {
            return serviceRepository.findById(String.valueOf(jsonParameter.get(WebMapperKey.VALUE.getKey())));
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
     * @param instance The newly created component instance.
     * @param map      The map of component data.
     */
    protected void setComponentSpecifica(IgorComponent instance, Map<String, Object> map) {
        if (map.containsKey(WebMapperKey.ID.getKey())) {
            instance.setId(String.valueOf(map.get(WebMapperKey.ID.getKey())));
        }
    }

}
