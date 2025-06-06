package com.arassec.igor.persistence.mapper;

import com.arassec.igor.application.util.IgorComponentUtil;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.persistence.security.SecurityProvider;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Serializer for igor components in the persistence module.
 */
public class IgorComponentPersistenceSerializer extends StdSerializer<IgorComponent> {

    /**
     * The security provider to encrypt secured parameter values.
     */
    private final transient SecurityProvider securityProvider;

    /**
     * Utility for handling igor components.
     */
    private final transient IgorComponentUtil igorComponentUtil;

    /**
     * Creates a new serializer instance.
     *
     * @param securityProvider  The security provider to handle secured parameter values.
     * @param igorComponentUtil Igor's component util.
     */
    public IgorComponentPersistenceSerializer(SecurityProvider securityProvider, IgorComponentUtil igorComponentUtil) {
        super(IgorComponent.class);
        this.securityProvider = securityProvider;
        this.igorComponentUtil = igorComponentUtil;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(IgorComponent instance, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField(PersistenceMapperKey.ID.getKey(), instance.getId());
        jsonGenerator.writeStringField(PersistenceMapperKey.TYPE_ID.getKey(), igorComponentUtil.getTypeId(instance));
        if (instance instanceof Connector connector) {
            jsonGenerator.writeStringField(PersistenceMapperKey.NAME.getKey(), connector.getName());
        }
        if (instance instanceof Action action) {
            jsonGenerator.writeStringField(PersistenceMapperKey.NAME.getKey(), action.getName());
            jsonGenerator.writeStringField(PersistenceMapperKey.DESCRIPTION.getKey(), action.getDescription());
            jsonGenerator.writeBooleanField(PersistenceMapperKey.ACTIVE.getKey(), action.isActive());
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
        if (value instanceof String stringValue && !StringUtils.hasText(stringValue)) {
            return;
        }
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField(PersistenceMapperKey.NAME.getKey(), field.getName());
        if (value instanceof Connector connector) {
            jsonGenerator.writeStringField(PersistenceMapperKey.VALUE.getKey(), connector.getId());
            jsonGenerator.writeBooleanField(PersistenceMapperKey.CONNECTOR.getKey(), true);
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
        var igorParam = field.getAnnotation(IgorParam.class);
        return Objects.requireNonNull(igorParam).secured() && field.getType().isAssignableFrom(String.class);
    }

}
