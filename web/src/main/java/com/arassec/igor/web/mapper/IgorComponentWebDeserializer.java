package com.arassec.igor.web.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.web.simulation.ServiceProxy;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Proxy;
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

        Map map = deserializationContext.readValue(jsonParser, Map.class);

        String typeId = getTypeId((Map) map.get(WebMapperKey.TYPE.getKey()));

        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = deserializeParameters((List<Map>) map.get(WebMapperKey.PARAMETERS.getKey()));

        T instance = createInstance(typeId, parameters, simulationMode);

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
    private Map<String, Object> deserializeParameters(List<Map> parameters) {
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
    private Service deserializeServiceParameter(Map jsonParameter) {
        if (simulationMode) {
            String serviceId = String.valueOf(jsonParameter.get(WebMapperKey.VALUE.getKey()));
            Service service = serviceRepository.findById(serviceId);
            if (service == null) {
                throw new IllegalArgumentException("No service with ID " + serviceId + " found!");
            }
            ServiceProxy serviceProxy = new ServiceProxy(service);
            try {
                return (Service) Proxy.newProxyInstance(IgorComponentWebDeserializer.class.getClassLoader(),
                        new Class[]{Class.forName(String.valueOf(jsonParameter.get(WebMapperKey.SERVICE_CLASS.getKey())))},
                        serviceProxy);
            } catch (ClassNotFoundException e) {
                throw new ServiceException("Unknown service class: " + jsonParameter.get(WebMapperKey.SERVICE_CLASS.getKey()));
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
    private String getTypeId(Map map) {
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
    private void setComponentSpecifica(IgorComponent instance, Map map) {
        if (map.containsKey(WebMapperKey.ID.getKey())) {
            instance.setId(String.valueOf(map.get(WebMapperKey.ID.getKey())));
        }
        if (instance instanceof Service) {
            if (map.containsKey(WebMapperKey.ID.getKey())) {
                instance.setId(String.valueOf(map.get(WebMapperKey.ID.getKey())));
            }
            ((Service) instance).setName(String.valueOf(map.get(WebMapperKey.NAME.getKey())));
        } else if (instance instanceof Action) {
            ((Action) instance).setActive((boolean) map.get(WebMapperKey.ACTIVE.getKey()));
        }
    }

}
