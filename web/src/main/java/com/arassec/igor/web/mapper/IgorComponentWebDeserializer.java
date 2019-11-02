package com.arassec.igor.web.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.core.util.ApplicationContextProvider;
import com.arassec.igor.web.simulation.ActionProxy;
import com.arassec.igor.web.simulation.ProviderProxy;
import com.arassec.igor.web.simulation.ServiceProxy;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Deserializer for igor components in the web layer.
 */
public class IgorComponentWebDeserializer<T extends IgorComponent> extends StdDeserializer<T> implements WebMapperKeyAware {

    /**
     * The igor component registry.
     */
    private final IgorComponentRegistry igorComponentRegistry;

    /**
     * The {@link ServiceRepository} to load services as parameter values.
     */
    private final ServiceRepository serviceRepository;

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
    public IgorComponentWebDeserializer(Class<T> clazz, IgorComponentRegistry igorComponentRegistry,
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

        Map<String, Object> map = deserializationContext.readValue(jsonParser, Map.class);

        String typeId = getTypeId((Map<String, Object>) map.get(TYPE));

        Optional<IgorComponent> classOptional = igorComponentRegistry.getInstance(typeId);
        if (classOptional.isEmpty()) {
            throw new IllegalStateException("Unknown type ID: " + typeId);
        }

        IgorComponent igorComponent = classOptional.get();
        T instance = (T) ApplicationContextProvider.getIgorComponent(igorComponent.getClass(), typeId);

        if (map.containsKey(ID)) {
            instance.setId(String.valueOf(map.get(ID)));
        }

        setComponentSpecifica(instance, map);

        List<Map<String, Object>> parameters = (List<Map<String, Object>>) map.get(PARAMETERS);
        if (parameters != null && !parameters.isEmpty()) {
            instance.getClass().getFields();

            ReflectionUtils.doWithFields(instance.getClass(), field -> {
                ReflectionUtils.makeAccessible(field);
                if (field.isAnnotationPresent(IgorParam.class)) {
                    Map<String, Object> parameter = getParameter(field.getName(), parameters);
                    if (parameter == null) {
                        return;
                    }
                    if (parameter.containsKey(SERVICE) && (boolean) parameter.get(SERVICE) && serviceRepository != null) {
                        if (simulationMode) {
                            String serviceId = String.valueOf(parameter.get(VALUE));
                            Service service = serviceRepository.findById(serviceId);
                            if (service == null) {
                                throw new IllegalArgumentException("No service with ID " + serviceId + " found!");
                            }
                            ServiceProxy serviceProxy = new ServiceProxy(service);
                            field.set(instance, Proxy.newProxyInstance(IgorComponentWebDeserializer.class.getClassLoader(),
                                    new Class[]{field.getType()}, serviceProxy));
                        } else {
                            field.set(instance, serviceRepository.findById(String.valueOf(parameter.get(VALUE))));
                        }
                    } else {
                        Object value = parameter.get(VALUE);
                        if (value != null) {
                            if (value instanceof String && StringUtils.isEmpty(value)) {
                                return;
                            }
                            field.set(instance, value);
                        }
                    }
                }
            });
        }

        if (instance instanceof Provider && simulationMode) {
            return (T) new ProviderProxy((Provider) instance);
        } else if (instance instanceof Action && simulationMode) {
            return (T) new ActionProxy((Action) instance);
        } else {
            return instance;
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
        Object type = map.get(KEY);
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
    private void setComponentSpecifica(IgorComponent instance, Map<String, Object> map) {
        if (instance instanceof Service) {
            if (map.containsKey(ID)) {
                instance.setId(String.valueOf(map.get(ID)));
            }
            ((Service) instance).setName(String.valueOf(map.get(NAME)));
        } else if (instance instanceof Action) {
            ((Action) instance).setActive((boolean) map.get(ACTIVE));
        }
    }

    /**
     * Returns the parameter with the given name or {@code null}, if none could be found.
     *
     * @param name       The parameter's name.
     * @param parameters All parameters of the component.
     *
     * @return The parameter as Map or {@code null}, if none with the given name exists.
     */
    private Map<String, Object> getParameter(String name, List<Map<String, Object>> parameters) {
        for (Map<String, Object> parameter : parameters) {
            if (parameter.containsKey(NAME) && name.equals(parameter.get(NAME))) {
                return parameter;
            }
        }
        return null;
    }

}
