package com.arassec.igor.web.mapper;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.web.model.KeyLabelStore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * Serializer for igor components in the web layer.
 */
public class IgorComponentWebSerializer extends StdSerializer<IgorComponent> implements WebMapperKeyAware {

    /**
     * The message source for I18N.
     */
    private final MessageSource messageSource;

    /**
     * Creates a new serializer instance.
     *
     * @param messageSource         The message source for I18N.
     */
    public IgorComponentWebSerializer(MessageSource messageSource) {
        super(IgorComponent.class);
        this.messageSource = messageSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(IgorComponent instance, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        if (instance instanceof Service) {
            if (((Service) instance).getId() != null) {
                jsonGenerator.writeNumberField(ID, ((Service) instance).getId());
            }
            if (((Service) instance).getName() != null) {
                jsonGenerator.writeStringField(NAME, ((Service) instance).getName());
            }
        }
        if (instance instanceof Action) {
            jsonGenerator.writeBooleanField(ACTIVE, ((Action) instance).isActive());
        }
        writeKeyLabelStore(jsonGenerator, CATEGORY,
                new KeyLabelStore(instance.getCategoryId(), messageSource.getMessage(instance.getCategoryId(), null,
                        LocaleContextHolder.getLocale())));
        writeKeyLabelStore(jsonGenerator, TYPE,
                new KeyLabelStore(instance.getTypeId(), messageSource.getMessage(instance.getTypeId(), null,
                        LocaleContextHolder.getLocale())));
        writeParameters(jsonGenerator, instance);
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
    private void writeKeyLabelStore(JsonGenerator jsonGenerator, String name, KeyLabelStore keyLabelStore) throws IOException {
        jsonGenerator.writeObjectFieldStart(name);
        jsonGenerator.writeStringField(KEY, keyLabelStore.getKey());
        jsonGenerator.writeStringField(VALUE, keyLabelStore.getValue());
        jsonGenerator.writeEndObject();
    }

    /**
     * Writes the component's parameters to the serialized json.
     *
     * @param jsonGenerator The json generator.
     * @param instance      The component instance.
     *
     * @throws IOException If parameters could not be written.
     */
    private void writeParameters(JsonGenerator jsonGenerator, IgorComponent instance) throws IOException {
        jsonGenerator.writeArrayFieldStart(PARAMETERS);

        List<Field> fields = new LinkedList<>();
        ReflectionUtils.doWithFields(instance.getClass(), field -> {
            ReflectionUtils.makeAccessible(field);
            if (field.isAnnotationPresent(IgorParam.class)) {
                IgorParam annotation = field.getAnnotation(IgorParam.class);
                if (annotation.configurable()) {
                    fields.add(field);
                }
            }
        });

        fields.sort((o1, o2) -> {
            Integer first = o1.getAnnotation(IgorParam.class).value();
            Integer second = o2.getAnnotation(IgorParam.class).value();
            return first.compareTo(second);
        });

        fields.forEach(field -> {
            try {
                Object value = field.get(instance);
                IgorParam annotation = field.getAnnotation(IgorParam.class);

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
                        jsonGenerator.writeObjectField(VALUE, null);
                        jsonGenerator.writeStringField(SERVICE_NAME, "");
                    }
                    jsonGenerator.writeBooleanField(SERVICE, true);
                } else {
                    if (value instanceof String && StringUtils.isEmpty(value)) {
                        jsonGenerator.writeObjectField(VALUE, null);
                    } else {
                        jsonGenerator.writeObjectField(VALUE, value);
                    }
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
