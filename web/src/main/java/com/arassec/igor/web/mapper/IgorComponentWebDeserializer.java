package com.arassec.igor.web.mapper;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.web.api.simulation.ActionProxy;
import com.arassec.igor.web.api.simulation.ProviderProxy;
import com.arassec.igor.web.api.simulation.ServiceProxy;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

/**
 * Deserializer for igor components in the web layer.
 *
 * @param <T> The type this deserializer can be used for.
 */
public class IgorComponentWebDeserializer<T> extends StdDeserializer<T> implements WebMapperKeyAware {

    /**
     * The {@link ServiceRepository} to load services as parameter values.
     */
    private ServiceRepository serviceRepository;

    /**
     * Can be set to {@true} to deserialize components in simulation mode, i.e. wrap proxies around them for job simulations.
     */
    private boolean simulationMode;

    /**
     * Creates a new deserializer instance.
     *
     * @param vc                The class this deserializer supports.
     * @param serviceRepository The service repository to load services from.
     * @param simulationMode    Set to {@code true} if the resulting  components are used during simulated job runs.
     */
    public IgorComponentWebDeserializer(Class<?> vc, ServiceRepository serviceRepository, boolean simulationMode) {
        super(vc);
        this.serviceRepository = serviceRepository;
        this.simulationMode = simulationMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        Map<String, Object> map = deserializationContext.readValue(jsonParser, Map.class);

        Map<String, Object> typeMap = (Map<String, Object>) map.get(TYPE);

        try {
            T instance = (T) Class.forName(String.valueOf(typeMap.get(KEY))).getConstructor().newInstance();

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
                                Long serviceId = Long.valueOf((Integer) parameter.get(VALUE));
                                Service service = serviceRepository.findById(serviceId);
                                if (service == null) {
                                    throw new IllegalArgumentException("No service with ID " + serviceId + " found!");
                                }
                                ServiceProxy serviceProxy = new ServiceProxy(service);
                                field.set(instance, Proxy.newProxyInstance(IgorComponentWebDeserializer.class.getClassLoader(),
                                        new Class[]{field.getType()}, serviceProxy));
                            } else {
                                field.set(instance, serviceRepository.findById((Long.valueOf((Integer) parameter.get(VALUE)))));
                            }
                        } else {
                            field.set(instance, parameter.get(VALUE));
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

        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not deserialize igor component!", e);
        }
    }

    /**
     * Sets component specific properties using the provided raw data.
     *
     * @param instance The newly created component instance.
     * @param map      The map of component data.
     */
    private void setComponentSpecifica(T instance, Map<String, Object> map) {
        if (instance instanceof Service) {
            if (map.containsKey(ID)) {
                ((Service) instance).setId(Long.valueOf(String.valueOf(map.get(ID))));
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
