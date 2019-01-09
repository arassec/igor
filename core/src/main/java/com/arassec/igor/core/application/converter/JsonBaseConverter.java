package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.application.factory.ModelDefinition;
import com.github.openjson.JSONObject;

/**
 * Base class for JSON converters.
 */
public abstract class JsonBaseConverter {

    /**
     * Converts a {@link ModelDefinition} to JSON.
     *
     * @param modelDefinition The model definition to convert.
     * @return A {@link JSONObject} with the model definition's data.
     */
    protected JSONObject convert(ModelDefinition modelDefinition) {
        JSONObject result = new JSONObject();
        result.put(JsonKeys.TYPE, modelDefinition.getType());
        result.put(JsonKeys.LABEL, modelDefinition.getLabel());
        return result;
    }

    /**
     * Converts a {@link JSONObject} into a {@link ModelDefinition}.
     *
     * @param modelDefinition The model definition in JSON form.
     * @return A newly created {@link ModelDefinition} containint the JSON's data.
     */
    protected ModelDefinition convert(JSONObject modelDefinition) {
        ModelDefinition result = new ModelDefinition();
        result.setType(modelDefinition.getString(JsonKeys.TYPE));
        result.setLabel(modelDefinition.getString(JsonKeys.LABEL));
        return result;
    }

}
