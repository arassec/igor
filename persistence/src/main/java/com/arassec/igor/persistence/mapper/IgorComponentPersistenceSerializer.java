package com.arassec.igor.persistence.mapper;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.annotation.IgorParam;
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
public class IgorComponentPersistenceSerializer extends StdSerializer<IgorComponent> {

    /**
     * The security provider to encrypt secured parameter values.
     */
    private final transient SecurityProvider securityProvider;

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
        jsonGenerator.writeStringField(PersistenceMapperKey.ID.getKey(), instance.getId());
        jsonGenerator.writeStringField(PersistenceMapperKey.TYPE_ID.getKey(), instance.getTypeId());
        if (instance instanceof Service) {
            jsonGenerator.writeStringField(PersistenceMapperKey.NAME.getKey(), ((Service) instance).getName());
        }
        if (instance instanceof Action) {
            jsonGenerator.writeStringField(PersistenceMapperKey.NAME.getKey(), ((Action) instance).getName());
            jsonGenerator.writeBooleanField(PersistenceMapperKey.ACTIVE.getKey(), ((Action) instance).isActive());
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
        jsonGenerator.writeArrayFieldStart(PersistenceMapperKey.PARAMETERS.getKey());

        ReflectionUtils.doWithFields(instance.getClass(), field -> {
            ReflectionUtils.makeAccessible(field);
            if (field.isAnnotationPresent(IgorParam.class)) {
                try {
                    serializeParameter(instance, jsonGenerator, field);
                } catch (IOException e) {
                    throw new IllegalStateException("Could not convert parameter to JSON!", e);
                }
            }
        });

        jsonGenerator.writeEndArray();
    }

    /**
     * Serializes a single parameter of the component.
     *
     * @param instance      The component instance.
     * @param jsonGenerator The JSON generator to write the parameter to.
     * @param field         The parameter to serialize.
     *
     * @throws IOException If the parameter could not be serialized.
     */
    private void serializeParameter(IgorComponent instance, JsonGenerator jsonGenerator, Field field) throws IOException {
        Object value = getFieldValue(instance, field);
        if (value == null) {
            return;
        }
        if (value instanceof String && StringUtils.isEmpty(value)) {
            return;
        }
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField(PersistenceMapperKey.NAME.getKey(), field.getName());
        if (Service.class.isAssignableFrom(field.getType()) && value instanceof Service) {
            jsonGenerator.writeStringField(PersistenceMapperKey.VALUE.getKey(), ((Service) value).getId());
            jsonGenerator.writeBooleanField(PersistenceMapperKey.SERVICE.getKey(), true);
        } else {
            jsonGenerator.writeObjectField(PersistenceMapperKey.VALUE.getKey(), value);
            if (value instanceof String && isSecured(field)) {
                jsonGenerator.writeBooleanField(PersistenceMapperKey.SECURED.getKey(), true);
            }
        }
        jsonGenerator.writeEndObject();
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
                ReflectionUtils.makeAccessible(field);
                if (isSecured(field)) {
                    result = securityProvider.encrypt(instance.getId(), field.getName(), (String) field.get(instance));
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
