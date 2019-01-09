package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.application.factory.ProviderFactory;
import com.arassec.igor.core.model.provider.Provider;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JsonProviderConverter {

    /**
     * Converter for parameters.
     */
    @Autowired
    private JsonParameterConverter parameterConverter;

    @Autowired
    private ProviderFactory providerFactory;

    /**
     * Converts the provided JSON into a {@link Provider}.
     *
     * @param providerJson  The JSON with provider data.
     * @param applySecurity Set to {@code true} to encrypt secured properties. If set to {@code false}, secured
     *                      properties will be kept in cleartext form.
     * @return A newly created Provider instance.
     */
    public Provider convert(JSONObject providerJson, boolean applySecurity) {
        Map<String, Object> parameters = parameterConverter.convert(
                providerJson.getJSONArray(JsonKeys.PARAMETERS), applySecurity);
        return providerFactory.createInstance(providerJson.getString(JsonKeys.TYPE), parameters);
    }


    public JSONObject convert(Provider provider, boolean applySecurity) {
        JSONObject result = new JSONObject();
        result.put(JsonKeys.TYPE, provider.getClass().getName());
        result.put(JsonKeys.PARAMETERS, parameterConverter.convert(provider, applySecurity));
        return result;
    }

}
