package com.arassec.igor.core.application.converter;

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
    protected JSONObject convert(KeyLabelStore keyLabelStore) {
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
    protected KeyLabelStore convert(JSONObject modelDefinition) {
        KeyLabelStore result = new KeyLabelStore();
        result.setKey(modelDefinition.getString(JsonKeys.KEY));
        result.setLabel(modelDefinition.getString(JsonKeys.LABEL));
        return result;
    }

}
