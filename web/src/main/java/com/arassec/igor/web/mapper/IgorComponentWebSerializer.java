package com.arassec.igor.web.mapper;

import com.arassec.igor.application.registry.IgorComponentRegistry;
import com.arassec.igor.application.util.IgorComponentUtil;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.core.model.trigger.EventTrigger;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.web.model.KeyLabelStore;
import com.arassec.igor.web.util.DocumentationUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

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
     * Util for handling igor components.
     */
    private final transient IgorComponentUtil igorComponentUtil;

    /**
     * Creates a new serializer instance.
     *
     * @param messageSource         The message source for I18N.
     * @param igorComponentRegistry The registry for igor components.
     */
    public IgorComponentWebSerializer(MessageSource messageSource, IgorComponentRegistry igorComponentRegistry,
                                      IgorComponentUtil igorComponentUtil) {
        super(IgorComponent.class);
        this.messageSource = messageSource;
        this.igorComponentRegistry = igorComponentRegistry;
        this.igorComponentUtil = igorComponentUtil;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(IgorComponent instance, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField(WebMapperKey.ID.getKey(), instance.getId());
        if (instance instanceof Connector connector && connector.getName() != null) {
            jsonGenerator.writeStringField(WebMapperKey.NAME.getKey(), ((Connector) instance).getName());
        } else if (instance instanceof Action action) {
            if (action.getName() != null) {
                jsonGenerator.writeStringField(WebMapperKey.NAME.getKey(), action.getName());
            } else {
                jsonGenerator.writeStringField(WebMapperKey.NAME.getKey(), "");
            }
            if (((Action) instance).getDescription() != null) {
                jsonGenerator.writeStringField(WebMapperKey.DESCRIPTION.getKey(), action.getDescription());
            } else {
                jsonGenerator.writeStringField(WebMapperKey.DESCRIPTION.getKey(), "");
            }
            jsonGenerator.writeBooleanField(WebMapperKey.ACTIVE.getKey(), action.isActive());
        }
        String categoryId = igorComponentUtil.getCategoryId(instance);
        writeKeyLabelStore(jsonGenerator, WebMapperKey.CATEGORY.getKey(),
            new KeyLabelStore(categoryId, messageSource.getMessage(categoryId, null, LocaleContextHolder.getLocale())));
        writeTypeData(jsonGenerator, instance);
        writeParameters(jsonGenerator, instance);
        jsonGenerator.writeEndObject();
    }

    /**
     * Writes a {@link KeyLabelStore} to the serialized json.
     *
     * @param jsonGenerator The json generator.
     * @param name          The name of the key-label-store.
     * @param keyLabelStore The actual store to write.
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
     * @param instance      The component instance.
     * @throws IOException In case of serialization problems.
     */
    private void writeTypeData(JsonGenerator jsonGenerator, IgorComponent instance) throws IOException {
        String typeId = igorComponentUtil.getTypeId(instance);
        jsonGenerator.writeObjectFieldStart(WebMapperKey.TYPE.getKey());
        jsonGenerator.writeStringField(WebMapperKey.KEY.getKey(), typeId);
        jsonGenerator.writeStringField(WebMapperKey.VALUE.getKey(), messageSource.getMessage(typeId, null,
            LocaleContextHolder.getLocale()));
        jsonGenerator.writeBooleanField(WebMapperKey.DOCUMENTATION_AVAILABLE.getKey(),
            DocumentationUtil.isDocumentationAvailable(typeId, LocaleContextHolder.getLocale()));
        if (instance instanceof Action action) {
            jsonGenerator.writeBooleanField(WebMapperKey.SUPPORTS_EVENTS.getKey(), action.supportsEvents());
        } else if (instance instanceof EventTrigger) {
            jsonGenerator.writeBooleanField(WebMapperKey.SUPPORTS_EVENTS.getKey(), true);
        } else if (instance instanceof Trigger) {
            jsonGenerator.writeBooleanField(WebMapperKey.SUPPORTS_EVENTS.getKey(), false);
        } else if (instance instanceof Connector) {
            jsonGenerator.writeBooleanField(WebMapperKey.SUPPORTS_EVENTS.getKey(), false);
        }
        jsonGenerator.writeEndObject();
    }

    /**
     * Writes the component's parameters to the serialized json.
     *
     * @param jsonGenerator The json generator.
     * @param instance      The component instance.
     * @throws IOException If parameters could not be written.
     */
    private void writeParameters(JsonGenerator jsonGenerator, IgorComponent instance) throws IOException {
        jsonGenerator.writeArrayFieldStart(WebMapperKey.PARAMETERS.getKey());

        String typeId = igorComponentUtil.getTypeId(instance);

        List<Field> parameters = new LinkedList<>();
        ReflectionUtils.doWithFields(instance.getClass(), field -> {
            ReflectionUtils.makeAccessible(field);
            if (field.isAnnotationPresent(IgorParam.class)) {
                parameters.add(field);
            }
        });

        List<Field> advancedParameters = parameters.stream().filter(field -> {
            IgorParam annotation = field.getAnnotation(IgorParam.class);
            return annotation.advanced();
        }).sorted((o1, o2) -> {
            Integer first = o1.getAnnotation(IgorParam.class).sortIndex();
            Integer second = o2.getAnnotation(IgorParam.class).sortIndex();
            return first.compareTo(second);
        }).toList();

        parameters.removeAll(advancedParameters);
        parameters.sort((o1, o2) -> {
            Integer first = o1.getAnnotation(IgorParam.class).sortIndex();
            Integer second = o2.getAnnotation(IgorParam.class).sortIndex();
            return first.compareTo(second);
        });
        parameters.addAll(advancedParameters);

        parameters.forEach(field -> {
            try {
                Object value = field.get(instance);
                IgorParam annotation = field.getAnnotation(IgorParam.class);

                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField(WebMapperKey.NAME.getKey(), field.getName());

                String key = typeId + "." + field.getName();
                String displayName = messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
                if (key.equals(displayName)) {
                    displayName = igorComponentUtil.formatIgorParamName(field.getName());
                }
                jsonGenerator.writeStringField(WebMapperKey.DISPLAY_NAME.getKey(), displayName);

                writeMetaData(jsonGenerator, annotation, field);

                Map<String, Set<String>> candidates = igorComponentRegistry.getConnectorParameterCategoryAndType(field.getType());

                if (!candidates.isEmpty()) {
                    writeConnectorParameter(jsonGenerator, value, candidates);
                } else {
                    jsonGenerator.writeStringField(WebMapperKey.TYPE.getKey(), field.getType().getName());
                    if (value instanceof String stringValue && !StringUtils.hasText(stringValue)) {
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
     * Adds additional data to the generated JSON, describing a parameter based on its annotation.
     *
     * @param jsonGenerator The JSON generator.
     * @param annotation    The {@link IgorParam} annotation.
     * @param field         The field annotated by the annotation.
     * @throws IOException If the JSON could not be written out.
     */
    private void writeMetaData(JsonGenerator jsonGenerator, IgorParam annotation, Field field) throws IOException {
        if (annotation.secured()) {
            jsonGenerator.writeBooleanField(WebMapperKey.SECURED.getKey(), true);
        }
        if (annotation.advanced()) {
            jsonGenerator.writeBooleanField(WebMapperKey.ADVANCED.getKey(), true);
        }
        if (!ParameterSubtype.NONE.equals(annotation.subtype())) {
            jsonGenerator.writeStringField(WebMapperKey.SUBTYPE.getKey(), annotation.subtype().name());
        }
        boolean required = (field.isAnnotationPresent(NotNull.class)
            || field.isAnnotationPresent(NotBlank.class) || field.getType().isPrimitive());
        if (required) {
            jsonGenerator.writeBooleanField(WebMapperKey.REQUIRED.getKey(), true);
        }
    }

    /**
     * Writes a connector parameter as JSON.
     *
     * @param jsonGenerator The json generator to create the JSON.
     * @param value         The value of the parameter.
     * @param candidates    The possible connector implementations that can be used as parameter values.
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
