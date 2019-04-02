package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.application.factory.TriggerFactory;
import com.arassec.igor.core.model.trigger.Trigger;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * JSON converter for {@link Trigger}s.
 */
@Component
public class JsonTriggerConverter extends JsonBaseConverter {

    /**
     * Converter for parameters.
     */
    @Autowired
    private JsonParametersConverter parameterConverter;

    /**
     * Factory for triggers.
     */
    @Autowired
    private TriggerFactory triggerFactory;

    /**
     * Converts the trigger JSON into a {@link Trigger}.
     *
     * @param triggerJson  The JSON with trigger data.
     * @param applySecurity Set to {@code true} to decrypt secured properties. If set to {@code false}, secured
     *                      properties will be kept in encrypted form.
     * @return A newly created Trigger instance.
     */
    public Trigger convert(JSONObject triggerJson, boolean applySecurity) {
        Map<String, Object> parameters = parameterConverter.convert(
                triggerJson.getJSONArray(JsonKeys.PARAMETERS), applySecurity);
        return triggerFactory.createInstance(triggerJson.getJSONObject(JsonKeys.TYPE).getString(JsonKeys.KEY), parameters);
    }

    /**
     * Converts the supplied {@link Trigger} into its JSON representation.
     *
     * @param trigger      The trigger to convert.
     * @param applySecurity Set to {@code true} to decrypt secured properties.
     * @param addVolatile   Set to {@code true} to add properties that only exist through annotations or could otherwise
     *                      be obtained, but can be added for convenience.
     * @return The trigger in JSON form.
     */
    public JSONObject convert(Trigger trigger, boolean applySecurity, boolean addVolatile) {
        return convertToStandardJson(triggerFactory, trigger, applySecurity, addVolatile);
    }

}