package com.arassec.igor.web.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.annotation.IgorParam;
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
public class IgorComponentWebSerializer extends StdSerializer<IgorComponent> {

    /**
     * The message source for I18N.
     */
    private final transient MessageSource messageSource;

    /**
     * The igor component registry.
     */
    private final transient IgorComponentRegistry igorComponentRegistry;

    /**
     * Creates a new serializer instance.
     *
     * @param messageSource The message source for I18N.
     */
    public IgorComponentWebSerializer(MessageSource messageSource, IgorComponentRegistry igorComponentRegistry) {
        super(IgorComponent.class);
        this.messageSource = messageSource;
        this.igorComponentRegistry = igorComponentRegistry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(IgorComponent instance, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField(WebMapperKey.ID.getKey(), instance.getId());
        if (instance instanceof Service && ((Service) instance).getName() != null) {
            jsonGenerator.writeStringField(WebMapperKey.NAME.getKey(), ((Service) instance).getName());
        }
        if (instance instanceof Action) {
            jsonGenerator.writeBooleanField(WebMapperKey.ACTIVE.getKey(), ((Action) instance).isActive());
        }
        writeKeyLabelStore(jsonGenerator,WebMapperKey. CATEGORY.getKey(),
                new KeyLabelStore(instance.getCategoryId(), messageSource.getMessage(instance.getCategoryId(), null,
                        LocaleContextHolder.getLocale())));
        writeKeyLabelStore(jsonGenerator, WebMapperKey.TYPE.getKey(),
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
        jsonGenerator.writeStringField(WebMapperKey.KEY.getKey(), keyLabelStore.getKey());
        jsonGenerator.writeStringField(WebMapperKey.VALUE.getKey(), keyLabelStore.getValue());
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
        jsonGenerator.writeArrayFieldStart(WebMapperKey.PARAMETERS.getKey());

        List<Field> fields = new LinkedList<>();
        ReflectionUtils.doWithFields(instance.getClass(), field -> {
            ReflectionUtils.makeAccessible(field);
            if (field.isAnnotationPresent(IgorParam.class) && !instance.getUnEditableProperties().contains(field.getName())) {
                fields.add(field);
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
                jsonGenerator.writeStringField(WebMapperKey.NAME.getKey(), field.getName());
                jsonGenerator.writeStringField(WebMapperKey.TYPE.getKey(), field.getType().getName());
                jsonGenerator.writeBooleanField(WebMapperKey.SECURED.getKey(), annotation.secured());
                jsonGenerator.writeBooleanField(WebMapperKey.OPTIONAL.getKey(), annotation.optional());
                jsonGenerator.writeStringField(WebMapperKey.SUBTYPE.getKey(), annotation.subtype().name());

                boolean isService = Service.class.isAssignableFrom(field.getType());
                if (isService) {
                    writeServiceParameter(jsonGenerator, field, value);
                } else {
                    if (value instanceof String && StringUtils.isEmpty(value)) {
                        jsonGenerator.writeObjectField(WebMapperKey.VALUE.getKey(), null);
                    } else {
                        jsonGenerator.writeObjectField(WebMapperKey.VALUE.getKey(), value);
                    }
                    jsonGenerator.writeBooleanField(WebMapperKey.SERVICE.getKey(), false);
                }
                jsonGenerator.writeEndObject();
            } catch (IOException | IllegalAccessException e) {
                throw new ServiceException("Could not convert parameter to JSON!", e);
            }
        });

        jsonGenerator.writeEndArray();
    }

    /**
     * Writes a service parameter as JSON.
     *
     * @param jsonGenerator The json generator to create the JSON.
     * @param field         The service parameter of the component.
     * @param value         The value of the parameter.
     *
     * @throws IOException If the parameter could not be written.
     */
    private void writeServiceParameter(JsonGenerator jsonGenerator, Field field, Object value) throws IOException {
        jsonGenerator.writeStringField(WebMapperKey.TYPE.getKey(), igorComponentRegistry.getCagetoryOfServiceInterface(field.getType()));
        jsonGenerator.writeStringField(WebMapperKey.SERVICE_CLASS.getKey(), field.getType().getName());
        if (value != null) {
            jsonGenerator.writeStringField(WebMapperKey.VALUE.getKey(), ((Service) value).getId());
            jsonGenerator.writeStringField(WebMapperKey.SERVICE_NAME.getKey(), ((Service) value).getName());
        } else {
            jsonGenerator.writeObjectField(WebMapperKey.VALUE.getKey(), null);
            jsonGenerator.writeStringField(WebMapperKey.SERVICE_NAME.getKey(), "");
        }
        jsonGenerator.writeBooleanField(WebMapperKey.SERVICE.getKey(), true);
    }

}
