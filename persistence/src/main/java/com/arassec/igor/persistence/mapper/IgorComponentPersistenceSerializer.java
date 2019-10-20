package com.arassec.igor.persistence.mapper;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.persistence.security.SecurityProvider;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Serializer for igor components in the persistence module.
 */
public class IgorComponentPersistenceSerializer extends StdSerializer<IgorComponent> implements PersistenceMapperKeyAware {

    /**
     * The security provider to encrypt secured parameter values.
     */
    private final SecurityProvider securityProvider;

    /**
     * Creates a new serializer instance.
     *
     * @param securityProvider The security provider to handle secured parameter values.
     */
    public IgorComponentPersistenceSerializer(SecurityProvider securityProvider) {
        super(IgorComponent.class);
        this.securityProvider = securityProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(IgorComponent instance, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField(TYPE_ID, instance.getTypeId());
        if (instance instanceof Action) {
            jsonGenerator.writeBooleanField(ACTIVE, ((Action) instance).isActive());
        }
        serializeParameters(instance, jsonGenerator);
        jsonGenerator.writeEndObject();

    }

    /**
     * Serializes the component's parameters.
     *
     * @param instance      The component instance to serialize.
     * @param jsonGenerator The JSON generator.
     *
     * @throws IOException In case of serialization errors.
     */
    private void serializeParameters(IgorComponent instance, JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeArrayFieldStart(PARAMETERS);

        ReflectionUtils.doWithFields(instance.getClass(), field -> {
            ReflectionUtils.makeAccessible(field);
            if (field.isAnnotationPresent(IgorParam.class)) {
                try {
                    Object value = getFieldValue(instance, field);
                    if (value == null) {
                        return;
                    }
                    if (value instanceof String && StringUtils.isEmpty(value)) {
                        return;
                    }
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField(NAME, field.getName());
                    if (Service.class.isAssignableFrom(field.getType()) && value instanceof Service) {
                        jsonGenerator.writeNumberField(VALUE, ((Service) value).getId());
                        jsonGenerator.writeBooleanField(SERVICE, true);
                    } else {
                        jsonGenerator.writeObjectField(VALUE, value);
                        if (value instanceof String && isSecured(field)) {
                            jsonGenerator.writeBooleanField(SECURED, true);
                        }
                    }
                    jsonGenerator.writeEndObject();
                } catch (IOException e) {
                    throw new IllegalStateException("Could not convert parameter to JSON!", e);
                }
            }
        });

        jsonGenerator.writeEndArray();
    }


    /**
     * Returns the value of the provided instance's property.
     *
     * @param instance The instance of a class to get the field's value from.
     * @param field    The property of the instance to get the value from.
     *
     * @return The propertie's value.
     */
    private Object getFieldValue(IgorComponent instance, Field field) {
        if (field != null) {
            try {
                Object result;
                field.setAccessible(true);
                if (isSecured(field)) {
                    // TODO: Use instance.getId() as soon as it's implemented...
                    result = securityProvider.encrypt(null, field.getName(), (String) field.get(instance));
                } else {
                    result = field.get(instance);
                }
                field.setAccessible(false);
                return result;
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Could not read property value!", e);
            }
        }
        return null;
    }

    /**
     * Indicates whether a property is secured or not.
     *
     * @param field The property to check.
     *
     * @return {@code true}, if the property is secured, {@code false} otherwise.
     */
    private boolean isSecured(Field field) {
        IgorParam igorParam = field.getAnnotation(IgorParam.class);
        return igorParam.secured() && field.getType().isAssignableFrom(String.class);
    }

}
