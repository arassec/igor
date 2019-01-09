package com.arassec.igor.web.api;

import com.arassec.igor.core.application.ProviderManager;
import com.arassec.igor.core.application.converter.JsonParameterConverter;
import com.arassec.igor.core.application.factory.ModelDefinition;
import com.github.openjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Set;

/**
 * REST controller for {@link com.arassec.igor.core.model.provider.Provider}s.
 */
public class ProviderRestController extends BaseRestController {

    /**
     * Manager for providers.
     */
    @Autowired
    private ProviderManager providerManager;

    /**
     * Converter for parameters to and from JSON.
     */
    @Autowired
    private JsonParameterConverter jsonParameterConverter;

    /**
     * Returns all action categories as {@link ModelDefinition}s.
     *
     * @return Set of all available action categories.
     */
    @GetMapping("/category/action")
    public Set<ModelDefinition> getActionCategories() {
        return providerManager.getCategories();
    }

    /**
     * Returns all action types of a certain category as {@link ModelDefinition}s.
     *
     * @param category The action category to use.
     * @return Set of action types.
     */
    @GetMapping("/type/action/{category}")
    public Set<ModelDefinition> getActionTypes(@PathVariable("category") String category) {
        return providerManager.getTypesOfCategory(category);
    }

    /**
     * Returns all configuration parameters of an action type.
     *
     * @param type The type to get parameters for.
     * @return List of parameters.
     */
    @GetMapping("/parameters/action/{type}")
    public String getActionParameters(@PathVariable("type") String type) {
        JSONArray parameters = jsonParameterConverter.convert(providerManager.createProvider(type, null), false);
        return parameters.toString();
    }

}
