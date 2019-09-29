package com.arassec.igor.persistence.mapper;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.repository.ServiceRepository;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Deserializer for igor components that converts JSON into components.
 *
 * @param <T> The component this deserializer is used for.
 */
@Slf4j
public class IgorComponentPersistenceDeserializer<T> extends StdDeserializer<T> implements PersistenceMapperKeyAware {

    /**
     * Encryption util for secured parameters that should be decrypted during deserialization.
     */
    private EncryptionUtil encryptionUtil;

    /**
     * The {@link ServiceRepository} to load services as parameter values. Can be {@code null} if no services should be loaded.
     */
    private ServiceRepository serviceRepository;

    /**
     * Creates a new deserializer.
     *
     * @param vc                The component, this deserializer is used for.
     * @param serviceRepository The repository for services. Can be {@code null} to ignore services as parameter values.
     * @param encryptionUtil    The encryption utility to decrypt secured parameter values.
     */
    public IgorComponentPersistenceDeserializer(Class<?> vc, ServiceRepository serviceRepository, EncryptionUtil encryptionUtil) {
        super(vc);
        this.serviceRepository = serviceRepository;
        this.encryptionUtil = encryptionUtil;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        Map<String, Object> map = deserializationContext.readValue(jsonParser, Map.class);

        try {
            String type = getType(map);
            if (type == null) {
                return null;
            }

            T instance = (T) Class.forName(type).getConstructor().newInstance();

            setComponentSpecifica(instance, map);

            List<Map<String, Object>> parameters = (List<Map<String, Object>>) map.get(PARAMETERS);
            if (parameters != null && !parameters.isEmpty()) {
                ReflectionUtils.doWithFields(instance.getClass(), field -> {
                    ReflectionUtils.makeAccessible(field);
                    if (field.isAnnotationPresent(IgorParam.class)) {
                        Map<String, Object> parameter = getParameter(field.getName(), parameters);
                        if (parameter == null) {
                            return;
                        }
                        if (parameter.containsKey(SERVICE) && (boolean) parameter.get(SERVICE) && serviceRepository != null) {
                            field.set(instance, serviceRepository.findById((Long.valueOf((Integer) parameter.get(VALUE)))));
                        } else if (parameter.containsKey(SECURED) && (boolean) parameter.get(SECURED)) {
                            field.set(instance, encryptionUtil.decrypt(String.valueOf(parameter.get(VALUE))));
                        } else {
                            field.set(instance, parameter.get(VALUE));
                        }
                    }
                });
            }

            return instance;

        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not deserialize igor componente!", e);
        }
    }

    /**
     * Returns the type of the component.
     *
     * @param map The component as map.
     *
     * @return The type of the component.
     */
    private String getType(Map<String, Object> map) {
        Object type = map.get(TYPE);
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
    private void setComponentSpecifica(T instance, Map<String, Object> map) {
        if (instance instanceof Service) {
            if (map.containsKey(ID)) {
                ((Service) instance).setId(Long.valueOf(String.valueOf(map.get(ID))));
            }
            ((Service) instance).setName(String.valueOf(map.get(NAME)));
        } else if (instance instanceof Action) {
            if (map.containsKey(ACTIVE)) {
                ((Action) instance).setActive((boolean) map.get(ACTIVE));
            }
        }
    }

    /**
     * Returns the parameter of the component, or {@code null} if it doesn't exist.
     *
     * @param name       The parameter's name.
     * @param parameters The list of parameters of the component.
     *
     * @return The parameter as map or {@code null}, if it doesn't exist.
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
