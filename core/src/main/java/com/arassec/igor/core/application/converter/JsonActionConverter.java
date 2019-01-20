package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.application.factory.ActionFactory;
import com.arassec.igor.core.model.action.Action;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Converter for {@link Action}s.
 */
@Component
public class JsonActionConverter extends JsonBaseConverter {

    /**
     * Converter for parameters.
     */
    @Autowired
    private JsonParametersConverter parameterConverter;

    /**
     * Factory for actions.
     */
    @Autowired
    private ActionFactory actionFactory;

    /**
     * Converts actions in JSON form to their object instance.
     *
     * @param actionJson    The action in JSON form.
     * @param applySecurity Set to {@code true} to decrypt secured parameters.
     * @return A newly created {@link Action} instance.
     */
    public Action convert(JSONObject actionJson, boolean applySecurity) {
        return actionFactory.createInstance(actionJson.getJSONObject(JsonKeys.TYPE).getString(JsonKeys.KEY),
                parameterConverter.convert(actionJson.getJSONArray(JsonKeys.PARAMETERS), applySecurity));
    }

    /**
     * Converts an {@link Action} to JSON.
     *
     * @param action        The action to convert.
     * @param applySecurity Set to {@code true} to encrypt secured parameters.
     * @param addVolatile   Set to {@code true} to add properties that only exist through annotations or could otherwise
     *                      be obtained, but can be added for convenience.
     * @return The action in JSON form.
     */
    public JSONObject convert(Action action, boolean applySecurity, boolean addVolatile) {
        JSONObject result = new JSONObject();
        result.put(JsonKeys.CATEGORY, convert(actionFactory.getCategory(action)));
        result.put(JsonKeys.TYPE, convert(actionFactory.getType(action)));
        result.put(JsonKeys.PARAMETERS, parameterConverter.convert(action, applySecurity, addVolatile));
        return result;
    }

}
