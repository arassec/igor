package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.application.converter.simulation.ActionProxy;
import com.arassec.igor.core.application.factory.ActionFactory;
import com.arassec.igor.core.model.action.Action;
import com.github.openjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Converter for {@link Action}s.
 */
@Component
@RequiredArgsConstructor
public class JsonActionConverter extends JsonBaseConverter {

    /**
     * Factory for actions.
     */
    private final ActionFactory actionFactory;

    /**
     * Converter for parameters.
     */
    private final JsonServiceAwareParametersConverter parametersConverter;

    /**
     * Converts actions in JSON form to their object instance.
     *
     * @param actionJson The action in JSON form.
     * @param config     The converter configuration.
     *
     * @return A newly created {@link Action} instance.
     */
    public Action convert(JSONObject actionJson, ConverterConfig config) {
        Action action = actionFactory.createInstance(actionJson.getJSONObject(JsonKeys.TYPE).getString(JsonKeys.KEY),
                parametersConverter.convert(actionJson.getJSONArray(JsonKeys.PARAMETERS), config));
        if (config.getSimulationDataCollector() != null) {
            ActionProxy actionProxy = new ActionProxy(action);
            config.getSimulationDataCollector().getActionProxies()
                    .get(config.getSimulationDataCollector().getCurIndex()).add(actionProxy);
            return actionProxy;
        }
        return action;
    }

    /**
     * Converts an {@link Action} to JSON.
     *
     * @param action The action to convert.
     * @param config The converter configuration.
     *
     * @return The action in JSON form.
     */
    public JSONObject convert(Action action, ConverterConfig config) {
        return convertToStandardJson(actionFactory, action, parametersConverter, config);
    }

}
