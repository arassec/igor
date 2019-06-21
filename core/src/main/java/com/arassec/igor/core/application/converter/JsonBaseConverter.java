package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.application.factory.ModelFactory;
import com.arassec.igor.core.application.factory.util.KeyLabelStore;
import com.github.openjson.JSONObject;

/**
 * Base class for JSON converters.
 */
public abstract class JsonBaseConverter {

    /**
     * Converts a {@link KeyLabelStore} to JSON.
     *
     * @param keyLabelStore The model definition to convert.
     * @return A {@link JSONObject} with the model definition's data.
     */
    protected JSONObject convertKeyLabelStore(KeyLabelStore keyLabelStore) {
        JSONObject result = new JSONObject();
        result.put(JsonKeys.KEY, keyLabelStore.getKey());
        result.put(JsonKeys.LABEL, keyLabelStore.getLabel());
        return result;
    }

    /**
     * Converts a {@link JSONObject} into a {@link KeyLabelStore}.
     *
     * @param modelDefinition The model definition in JSON form.
     * @return A newly created {@link KeyLabelStore} containint the JSON's data.
     */
    protected KeyLabelStore convertKeyLabelStore(JSONObject modelDefinition) {
        KeyLabelStore result = new KeyLabelStore();
        result.setKey(modelDefinition.getString(JsonKeys.KEY));
        result.setLabel(modelDefinition.getString(JsonKeys.LABEL));
        return result;
    }

    /**
     * Converts a model into a "standard" category-type-parameters JSON object.
     *
     * @param factory       The model factory.
     * @param instance      The model instance.
     * @param applySecurity Set to {@code true} to apply encryption.
     * @param addVolatile   Set to {@code true} to add optional data for the GUI to the JSON.
     * @param <T>           The model's type.
     * @return A JSONObject representing the model.
     */
    protected <T> JSONObject convertToStandardJson(ModelFactory<T> factory, T instance, boolean applySecurity,
                                                   boolean addVolatile, JsonServiceAwareParametersConverter parametersConverter) {
        JSONObject result = new JSONObject();
        result.put(JsonKeys.CATEGORY, convertKeyLabelStore(factory.getCategory(instance)));
        result.put(JsonKeys.TYPE, convertKeyLabelStore(factory.getType(instance)));
        result.put(JsonKeys.PARAMETERS, parametersConverter.convert(instance, applySecurity, addVolatile));
        return result;
    }
}
