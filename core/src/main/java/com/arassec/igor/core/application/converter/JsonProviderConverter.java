package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.application.factory.ProviderFactory;
import com.arassec.igor.core.model.provider.Provider;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * JSON converter for {@link Provider}s.
 */
@Component
public class JsonProviderConverter extends JsonBaseConverter {

    /**
     * Converter for parameters.
     */
    @Autowired
    private JsonParametersConverter parameterConverter;

    /**
     * Factory for providers.
     */
    @Autowired
    private ProviderFactory providerFactory;

    /**
     * Converts the provided JSON into a {@link Provider}.
     *
     * @param providerJson  The JSON with provider data.
     * @param applySecurity Set to {@code true} to decrypt secured properties. If set to {@code false}, secured
     *                      properties will be kept in encrypted form.
     * @return A newly created Provider instance.
     */
    public Provider convert(JSONObject providerJson, boolean applySecurity) {
        Map<String, Object> parameters = parameterConverter.convert(
                providerJson.getJSONArray(JsonKeys.PARAMETERS), applySecurity);
        return providerFactory.createInstance(providerJson.getJSONObject(JsonKeys.TYPE).getString(JsonKeys.KEY), parameters);
    }

    /**
     * Converts the supplied {@link Provider} into its JSON representation.
     *
     * @param provider      The provider to convert.
     * @param applySecurity Set to {@code true} to decrypt secured properties.
     * @param addVolatile   Set to {@code true} to add properties that only exist through annotations or could otherwise
     *                      be obtained, but can be added for convenience.
     * @return The provider in JSON form.
     */
    public JSONObject convert(Provider provider, boolean applySecurity, boolean addVolatile) {
        JSONObject result = new JSONObject();
        result.put(JsonKeys.CATEGORY, convert(providerFactory.getCategory(provider)));
        result.put(JsonKeys.TYPE, convert(providerFactory.getType(provider)));
        result.put(JsonKeys.PARAMETERS, parameterConverter.convert(provider, applySecurity, addVolatile));
        return result;
    }

}
