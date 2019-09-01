package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.application.converter.simulation.ProviderProxy;
import com.arassec.igor.core.application.converter.simulation.SimulationDataCollector;
import com.arassec.igor.core.application.factory.ProviderFactory;
import com.arassec.igor.core.model.provider.Provider;
import com.github.openjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Map;

/**
 * JSON converter for {@link Provider}s.
 */
@Component
@RequiredArgsConstructor
public class JsonProviderConverter extends JsonBaseConverter {

    /**
     * Factory for providers.
     */
    private final ProviderFactory providerFactory;

    /**
     * Converter for parameters.
     */
    private final JsonServiceAwareParametersConverter parametersConverter;

    /**
     * Converts the provided JSON into a {@link Provider}.
     *
     * @param providerJson The JSON with provider data.
     * @param config       The converter configuration.
     *
     * @return A newly created Provider instance.
     */
    public Provider convert(JSONObject providerJson, ConverterConfig config) {
        Map<String, Object> parameters = parametersConverter.convert(
                providerJson.getJSONArray(JsonKeys.PARAMETERS), config);
        Provider provider = providerFactory.createInstance(providerJson.getJSONObject(JsonKeys.TYPE).getString(JsonKeys.KEY), parameters);
        if (config.getSimulationDataCollector() != null) {
            ProviderProxy providerProxy = new ProviderProxy(provider);
            config.getSimulationDataCollector().getProviderProxies().add(providerProxy);
            config.getSimulationDataCollector().getActionProxies().put(config.getSimulationDataCollector().getCurIndex(),
                    new LinkedList<>());
            return providerProxy;
        }
        return provider;
    }

    /**
     * Converts the supplied {@link Provider} into its JSON representation.
     *
     * @param provider      The provider to convert.
     * @param config     The converter configuration.
     *
     * @return The provider in JSON form.
     */
    public JSONObject convert(Provider provider, ConverterConfig config) {
        return convertToStandardJson(providerFactory, provider, parametersConverter, config);
    }

}
