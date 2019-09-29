package com.arassec.igor.persistence.mapper;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.service.Service;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Serializer for igor components in the persistence module.
 *
 * @param <T> The igor component this serializer is used for.
 */
public class IgorComponentPersistenceSerializer<T> extends StdSerializer<T> implements PersistenceMapperKeyAware {

    /**
     * The encryption util to encrypt secured parameter values.
     */
    private final EncryptionUtil encryptionUtil;

    /**
     * Creates a new serializer instance.
     *
     * @param t              The igor component, this serializer is used for.
     * @param encryptionUtil The encryption util to handle secured parameter values.
     */
    public IgorComponentPersistenceSerializer(Class<T> t, EncryptionUtil encryptionUtil) {
        super(t);
        this.encryptionUtil = encryptionUtil;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(T t, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField(TYPE, t.getClass().getName());
        if (t instanceof Action) {
            jsonGenerator.writeBooleanField(ACTIVE, ((Action) t).isActive());
        }
        serializeParameters(jsonGenerator, t);
        jsonGenerator.writeEndObject();

    }

    /**
     * Serializes the component's parameters.
     *
     * @param jsonGenerator The JSON generator.
     * @param instance      The component instance to serialize.
     * @param <T>           The instance type.
     *
     * @throws IOException In case of serialization errors.
     */
    <T> void serializeParameters(JsonGenerator jsonGenerator, T instance) throws IOException {
        jsonGenerator.writeArrayFieldStart(PARAMETERS);

        ReflectionUtils.doWithFields(instance.getClass(), field -> {
            ReflectionUtils.makeAccessible(field);
            if (field.isAnnotationPresent(IgorParam.class)) {
                try {
                    Object value = getFieldValue(instance, field);
                    if (value != null) {
                        jsonGenerator.writeStartObject();
                        jsonGenerator.writeStringField(NAME, field.getName());
                        boolean isService = Service.class.isAssignableFrom(field.getType());
                        if (isService) {
                            jsonGenerator.writeNumberField(VALUE, ((Service) value).getId());
                            jsonGenerator.writeBooleanField(SERVICE, true);
                        } else {
                            jsonGenerator.writeObjectField(VALUE, value);
                            if (value instanceof String && isSecured(field)) {
                                jsonGenerator.writeBooleanField(SECURED, true);
                            }
                        }
                        jsonGenerator.writeEndObject();
                    }
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
     * @param <T>      The instance type.
     *
     * @return The propertie's value.
     */
    private <T> Object getFieldValue(T instance, Field field) {
        if (field != null) {
            try {
                Object result;
                field.setAccessible(true);
                if (isSecured(field)) {
                    result = encryptionUtil.encrypt((String) field.get(instance));
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
