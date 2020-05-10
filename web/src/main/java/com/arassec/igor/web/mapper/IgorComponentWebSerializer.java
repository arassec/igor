package com.arassec.igor.web.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.web.model.KeyLabelStore;
import com.arassec.igor.web.util.DocumentationUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Serializer for igor components in the web layer.
 */
@Slf4j
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
     * @param messageSource         The message source for I18N.
     * @param igorComponentRegistry The registry for igor components.
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
        if (instance instanceof Connector && ((Connector) instance).getName() != null) {
            jsonGenerator.writeStringField(WebMapperKey.NAME.getKey(), ((Connector) instance).getName());
        }
        if (instance instanceof Action) {
            if (((Action) instance).getName() != null) {
                jsonGenerator.writeStringField(WebMapperKey.NAME.getKey(), ((Action) instance).getName());
            } else {
                jsonGenerator.writeStringField(WebMapperKey.NAME.getKey(), "");
            }
            jsonGenerator.writeBooleanField(WebMapperKey.ACTIVE.getKey(), ((Action) instance).isActive());
        }
        writeKeyLabelStore(jsonGenerator, WebMapperKey.CATEGORY.getKey(),
                new KeyLabelStore(instance.getCategoryId(), messageSource.getMessage(instance.getCategoryId(), null,
                        LocaleContextHolder.getLocale())));
        writeTypeData(jsonGenerator, WebMapperKey.TYPE.getKey(),
                instance.getTypeId(), messageSource.getMessage(instance.getTypeId(), null,
                        LocaleContextHolder.getLocale()));
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
     * Writes a {@link KeyLabelStore} to the serialized json.
     *
     * @param jsonGenerator The json generator.
     * @param name          The name of the key-label-store.
     * @param key           The key.
     * @param label         The label.
     *
     * @throws IOException In case of serialization problems.
     */
    private void writeTypeData(JsonGenerator jsonGenerator, String name, String key, String label) throws IOException {
        jsonGenerator.writeObjectFieldStart(name);
        jsonGenerator.writeStringField(WebMapperKey.KEY.getKey(), key);
        jsonGenerator.writeStringField(WebMapperKey.VALUE.getKey(), label);
        jsonGenerator.writeBooleanField(WebMapperKey.DOCUMENTATION_AVAILABLE.getKey(),
                DocumentationUtil.isDocumentationAvailable(key, LocaleContextHolder.getLocale()));
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
                jsonGenerator.writeBooleanField(WebMapperKey.SECURED.getKey(), annotation.secured());
                jsonGenerator.writeBooleanField(WebMapperKey.ADVANCED.getKey(), annotation.advanced());
                jsonGenerator.writeStringField(WebMapperKey.SUBTYPE.getKey(), annotation.subtype().name());
                jsonGenerator.writeBooleanField(WebMapperKey.REQUIRED.getKey(), (field.isAnnotationPresent(NotNull.class)
                        || field.isAnnotationPresent(NotBlank.class) || field.getType().isPrimitive()));

                Map<String, Set<String>> candidates = igorComponentRegistry.getConnectorParameterCategoryAndType(field.getType());

                if (!candidates.isEmpty()) {
                    writeConnectorParameter(jsonGenerator, value, candidates);
                } else {
                    jsonGenerator.writeStringField(WebMapperKey.TYPE.getKey(), field.getType().getName());
                    if (value instanceof String && StringUtils.isEmpty(value)) {
                        jsonGenerator.writeObjectField(WebMapperKey.VALUE.getKey(), null);
                    } else {
                        jsonGenerator.writeObjectField(WebMapperKey.VALUE.getKey(), value);
                    }
                    jsonGenerator.writeBooleanField(WebMapperKey.CONNECTOR.getKey(), false);
                }
                jsonGenerator.writeEndObject();
            } catch (IOException | IllegalAccessException e) {
                throw new IgorException("Could not convert parameter to JSON!", e);
            }
        });

        jsonGenerator.writeEndArray();
    }

    /**
     * Writes a connector parameter as JSON.
     *
     * @param jsonGenerator The json generator to create the JSON.
     * @param value         The value of the parameter.
     * @param candidates    The possible connector implementations that can be used as parameter values.
     *
     * @throws IOException If the parameter could not be written.
     */
    private void writeConnectorParameter(JsonGenerator jsonGenerator, Object value, Map<String, Set<String>> candidates) throws IOException {
        List<KeyLabelStore> categories = new LinkedList<>();
        candidates.keySet().forEach(candidate -> categories.add(new KeyLabelStore(candidate, messageSource.getMessage(candidate,
                null, LocaleContextHolder.getLocale()))));
        categories.sort(Comparator.comparing(KeyLabelStore::getValue)); // sort by label

        jsonGenerator.writeArrayFieldStart(WebMapperKey.CATEGORY_CANDIDATES.getKey());

        categories.forEach(category -> {
            Set<String> typeCandidates = candidates.get(category.getKey());
            List<KeyLabelStore> types = new LinkedList<>();
            typeCandidates.forEach(typeCandidate -> types.add(new KeyLabelStore(typeCandidate, messageSource.getMessage(typeCandidate, null,
                    LocaleContextHolder.getLocale()))));
            types.sort(Comparator.comparing(KeyLabelStore::getValue));

            try {
                jsonGenerator.writeStartObject();

                jsonGenerator.writeStringField(WebMapperKey.KEY.getKey(), category.getKey());
                jsonGenerator.writeStringField(WebMapperKey.VALUE.getKey(), category.getValue());

                jsonGenerator.writeArrayFieldStart(WebMapperKey.TYPE_CANDIDATES.getKey());
                types.forEach(type -> {
                    try {
                        jsonGenerator.writeStartObject();
                        jsonGenerator.writeStringField(WebMapperKey.KEY.getKey(), type.getKey());
                        jsonGenerator.writeStringField(WebMapperKey.VALUE.getKey(), type.getValue());
                        jsonGenerator.writeEndObject();
                    } catch (IOException e) {
                        log.error("Could not serialize type of connector parameter (" + type.getKey() + " / "
                                + type.getValue() + ")", e);
                    }
                });
                jsonGenerator.writeEndArray();

                jsonGenerator.writeEndObject();
            } catch (IOException e) {
                log.error("Could not serialize category of connector parameter (" + category.getKey() + " / " + category.getValue() + ")", e);
            }
        });

        jsonGenerator.writeEndArray();


        if (value != null) {
            jsonGenerator.writeStringField(WebMapperKey.VALUE.getKey(), ((Connector) value).getId());
            jsonGenerator.writeStringField(WebMapperKey.CONNECTOR_NAME.getKey(), ((Connector) value).getName());
        } else {
            jsonGenerator.writeObjectField(WebMapperKey.VALUE.getKey(), null);
            jsonGenerator.writeStringField(WebMapperKey.CONNECTOR_NAME.getKey(), "");
        }
        jsonGenerator.writeBooleanField(WebMapperKey.CONNECTOR.getKey(), true);
    }

}
