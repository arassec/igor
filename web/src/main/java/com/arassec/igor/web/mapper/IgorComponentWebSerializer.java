package com.arassec.igor.web.mapper;

import com.arassec.igor.core.application.factory.ModelFactory;
import com.arassec.igor.core.application.factory.util.KeyLabelStore;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.service.ServiceException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Serializer for igor components in the web layer.
 *
 * @param <T> The type of component this serializer processes.
 */
public class IgorComponentWebSerializer<T> extends StdSerializer<T> implements WebMapperKeyAware {

    /**
     * The model factory.
     */
    private ModelFactory<T> modelFactory;

    /**
     * Creates a new serializer instance.
     *
     * @param t            The type of components this serializer processes.
     * @param modelFactory The model factory.
     */
    public IgorComponentWebSerializer(Class<T> t, ModelFactory<T> modelFactory) {
        super(t);
        this.modelFactory = modelFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(T t, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        if (t instanceof Service) {
            if (((Service) t).getId() != null) {
                jsonGenerator.writeNumberField(ID, ((Service) t).getId());
            }
            if (((Service) t).getName() != null) {
                jsonGenerator.writeStringField(NAME, ((Service) t).getName());
            }
        }
        if (t instanceof Action) {
            jsonGenerator.writeBooleanField(ACTIVE, ((Action) t).isActive());
        }
        writeKeyLabelStore(jsonGenerator, CATEGORY, modelFactory.getCategory(t));
        writeKeyLabelStore(jsonGenerator, TYPE, modelFactory.getType(t));
        writeParameters(jsonGenerator, t);
        jsonGenerator.writeEndObject();
    }

    /**
     * Writes a {@link KeyLabelStore} to the serialized json.
     *
     * @param jsonGenerator The json generator.
     * @param name          The name of the key-label-store.
     * @param keyLabelStore The actual store to write.
     *
     * @throws IOException In case of serialization problems.
     */
    void writeKeyLabelStore(JsonGenerator jsonGenerator, String name, KeyLabelStore keyLabelStore) throws IOException {
        jsonGenerator.writeObjectFieldStart(name);
        jsonGenerator.writeStringField(KEY, keyLabelStore.getKey());
        jsonGenerator.writeStringField(LABEL, keyLabelStore.getLabel());
        jsonGenerator.writeEndObject();
    }

    /**
     * Writes the component's parameters to the serialized json.
     *
     * @param jsonGenerator The json generator.
     * @param instance      The component instance.
     * @param <T>           The component's type.
     *
     * @throws IOException
     */
    <T> void writeParameters(JsonGenerator jsonGenerator, T instance) throws IOException {
        jsonGenerator.writeArrayFieldStart(PARAMETERS);

        List<Field> fields = new LinkedList<>();
        ReflectionUtils.doWithFields(instance.getClass(), field -> {
            if (field.isAnnotationPresent(IgorParam.class)) {
                IgorParam annotation = field.getAnnotation(IgorParam.class);
                if (annotation.configurable()) {
                    fields.add(field);
                }
            }
        });

        Collections.sort(fields, (o1, o2) -> {
            Integer first = o1.getAnnotation(IgorParam.class).value();
            Integer second = o2.getAnnotation(IgorParam.class).value();
            return first.compareTo(second);
        });

        fields.stream().forEach(field -> {
            try {
                boolean accessibility = field.canAccess(instance);
                field.setAccessible(true);
                Object value = field.get(instance);
                IgorParam annotation = field.getAnnotation(IgorParam.class);
                field.setAccessible(accessibility);

                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField(NAME, field.getName());
                jsonGenerator.writeStringField(TYPE, field.getType().getName());
                jsonGenerator.writeBooleanField(SECURED, annotation.secured());
                jsonGenerator.writeBooleanField(OPTIONAL, annotation.optional());
                jsonGenerator.writeBooleanField(CONFIGURABLE, annotation.configurable());
                jsonGenerator.writeStringField(SUBTYPE, annotation.subtype().name());

                boolean isService = Service.class.isAssignableFrom(field.getType());
                if (isService) {
                    if (value != null) {
                        jsonGenerator.writeNumberField(VALUE, ((Service) value).getId());
                        jsonGenerator.writeStringField(SERVICE_NAME, ((Service) value).getName());
                    } else {
                        jsonGenerator.writeObjectField(VALUE, value);
                        jsonGenerator.writeStringField(SERVICE_NAME, "");
                    }
                    jsonGenerator.writeBooleanField(SERVICE, true);
                } else {
                    jsonGenerator.writeObjectField(VALUE, value);
                    jsonGenerator.writeBooleanField(SERVICE, false);

                }
                jsonGenerator.writeEndObject();
            } catch (IOException | IllegalAccessException e) {
                throw new ServiceException("Could not convert parameter to JSON!", e);
            }
        });

        jsonGenerator.writeEndArray();
    }

}
