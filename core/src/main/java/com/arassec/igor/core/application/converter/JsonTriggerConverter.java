package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.application.factory.TriggerFactory;
import com.arassec.igor.core.model.trigger.Trigger;
import com.github.openjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * JSON converter for {@link Trigger}s.
 */
@Component
@RequiredArgsConstructor
public class JsonTriggerConverter extends JsonBaseConverter {

    /**
     * Factory for triggers.
     */
    private final TriggerFactory triggerFactory;

    /**
     * Converter for parameters.
     */
    private final JsonServiceAwareParametersConverter parametersConverter;

    /**
     * Converts the trigger JSON into a {@link Trigger}.
     *
     * @param triggerJson The JSON with trigger data.
     * @param config      The converter configuration.
     *
     * @return A newly created Trigger instance.
     */
    public Trigger convert(JSONObject triggerJson, ConverterConfig config) {
        Map<String, Object> parameters = parametersConverter.convert(
                triggerJson.getJSONArray(JsonKeys.PARAMETERS), config);
        return triggerFactory.createInstance(triggerJson.getJSONObject(JsonKeys.TYPE).getString(JsonKeys.KEY), parameters);
    }

    /**
     * Converts the supplied {@link Trigger} into its JSON representation.
     *
     * @param trigger The trigger to convert.
     * @param config  The converter configuration.
     *
     * @return The trigger in JSON form.
     */
    public JSONObject convert(Trigger trigger, ConverterConfig config) {
        return convertToStandardJson(triggerFactory, trigger, parametersConverter, config);
    }

}
