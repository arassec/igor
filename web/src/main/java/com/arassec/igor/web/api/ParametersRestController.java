package com.arassec.igor.web.api;

import com.arassec.igor.core.application.ActionManager;
import com.arassec.igor.core.application.ProviderManager;
import com.arassec.igor.core.application.ServiceManager;
import com.arassec.igor.core.application.TriggerManager;
import com.arassec.igor.core.application.converter.JsonServiceAwareParametersConverter;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ParametersRestController {

    /**
     * The service manager.
     */
    private final ServiceManager serviceManager;

    /**
     * Manager for actions.
     */
    private final ActionManager actionManager;

    /**
     * Manager for providers.
     */
    private final ProviderManager providerManager;

    /**
     * Manager for triggers.
     */
    private final TriggerManager triggerManager;

    /**
     * Converter for parameters to and from JSON.
     */
    private final JsonServiceAwareParametersConverter parametersConverter;

    /**
     * Returns all configuration parameters of a service type.
     *
     * @param type The type to get parameters for.
     * @return List of parameters.
     */
    @GetMapping(value = "service/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getServiceParameters(@PathVariable("type") String type) {
        return parametersConverter.convert(
                serviceManager.createInstance(type, null), false, true).toString();
    }

    /**
     * Returns all configuration parameters of an action type.
     *
     * @param type The type to get parameters for.
     * @return List of parameters.
     */
    @GetMapping(value = "action/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getActionParameters(@PathVariable("type") String type) {
        return parametersConverter.convert(
                actionManager.createInstance(type, null), false, true).toString();
    }

    /**
     * Returns all configuration parameters of an action type.
     *
     * @param type The type to get parameters for.
     * @return List of parameters.
     */
    @GetMapping(value = "provider/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getProviderParameters(@PathVariable("type") String type) {
        return parametersConverter.convert(
                providerManager.createInstance(type, null), false, true).toString();
    }

    /**
     * Returns all configuration parameters of an action type.
     *
     * @param type The type to get parameters for.
     * @return List of parameters.
     */
    @GetMapping(value = "trigger/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getTriggerParameters(@PathVariable("type") String type) {
        return parametersConverter.convert(
                triggerManager.createInstance(type, null), false, true).toString();
    }

}
