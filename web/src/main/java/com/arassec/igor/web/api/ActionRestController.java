package com.arassec.igor.web.api;

import com.arassec.igor.core.application.ActionManager;
import com.arassec.igor.core.application.converter.JsonParameterConverter;
import com.arassec.igor.core.application.factory.ModelDefinition;
import com.github.openjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Set;

/**
 * REST-Controller for {@link com.arassec.igor.core.model.action.Action}s.
 */
public class ActionRestController extends BaseRestController {

    /**
     * Manager for actions.
     */
    @Autowired
    private ActionManager actionManager;

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
        return actionManager.getCategories();
    }

    /**
     * Returns all action types of a certain category as {@link ModelDefinition}s.
     *
     * @param category The action category to use.
     * @return Set of action types.
     */
    @GetMapping("/type/action/{category}")
    public Set<ModelDefinition> getActionTypes(@PathVariable("category") String category) {
        return actionManager.getTypesOfCategory(category);
    }

    /**
     * Returns all configuration parameters of an action type.
     *
     * @param type The type to get parameters for.
     * @return List of parameters.
     */
    @GetMapping("/parameters/action/{type}")
    public String getActionParameters(@PathVariable("type") String type) {
        JSONArray parameters = jsonParameterConverter.convert(actionManager.createAction(type, null), false);
        return parameters.toString();
    }

}
