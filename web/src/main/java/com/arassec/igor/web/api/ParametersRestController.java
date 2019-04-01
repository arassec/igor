package com.arassec.igor.web.api;

import com.arassec.igor.core.application.ActionManager;
import com.arassec.igor.core.application.ProviderManager;
import com.arassec.igor.core.application.ServiceManager;
import com.arassec.igor.core.application.TriggerManager;
import com.arassec.igor.core.application.converter.JsonParametersConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Controller for parameter requests.
 */
@RestController()
@RequestMapping("/api/parameters")
public class ParametersRestController {

    /**
     * The service manager.
     */
    @Autowired
    private ServiceManager serviceManager;

    /**
     * Manager for actions.
     */
    @Autowired
    private ActionManager actionManager;

    /**
     * Manager for providers.
     */
    @Autowired
    private ProviderManager providerManager;

    /**
     * Manager for triggers.
     */
    @Autowired
    private TriggerManager triggerManager;

    /**
     * Converter for parameters to and from JSON.
     */
    @Autowired
    private JsonParametersConverter jsonParametersConverter;

    /**
     * Returns all configuration parameters of a service type.
     *
     * @param type The type to get parameters for.
     * @return List of parameters.
     */
    @GetMapping(value = "service/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getServiceParameters(@PathVariable("type") String type) {
        return jsonParametersConverter.convert(
                serviceManager.createService(type, null), false, true).toString();
    }

    /**
     * Returns all configuration parameters of an action type.
     *
     * @param type The type to get parameters for.
     * @return List of parameters.
     */
    @GetMapping(value = "action/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getActionParameters(@PathVariable("type") String type) {
        return jsonParametersConverter.convert(
                actionManager.createAction(type, null), false, true).toString();
    }

    /**
     * Returns all configuration parameters of an action type.
     *
     * @param type The type to get parameters for.
     * @return List of parameters.
     */
    @GetMapping(value = "provider/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getProviderParameters(@PathVariable("type") String type) {
        return jsonParametersConverter.convert(
                providerManager.createProvider(type, null), false, true).toString();
    }

    /**
     * Returns all configuration parameters of an action type.
     *
     * @param type The type to get parameters for.
     * @return List of parameters.
     */
    @GetMapping(value = "trigger/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getTriggerParameters(@PathVariable("type") String type) {
        return jsonParametersConverter.convert(
                triggerManager.createTrigger(type, null), false, true).toString();
    }

}
