package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.application.factory.ActionFactory;
import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.action.Action;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JsonActionConverter {

    /**
     * Converter for parameters.
     */
    @Autowired
    private JsonParameterConverter parameterConverter;

    /**
     * Factory for actions.
     */
    @Autowired
    private ActionFactory actionFactory;

    public Action convert(JSONObject actionJson, boolean applySecurity) {
        return actionFactory.createInstance(actionJson.getString(JsonKeys.TYPE),
                parameterConverter.convert(actionJson.getJSONArray(JsonKeys.PARAMETERS), applySecurity));
    }

    public JSONObject convert(Action action, boolean applySecurity) {
        JSONObject result = new JSONObject();
        result.put(JsonKeys.TYPE, action.getClass().getName());
        result.put(JsonKeys.LABEL, action.getClass().getAnnotation(IgorAction.class).label());
        result.put(JsonKeys.PARAMETERS, parameterConverter.convert(action, applySecurity));
        return result;
    }

}
