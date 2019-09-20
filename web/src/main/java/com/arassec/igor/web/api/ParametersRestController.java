package com.arassec.igor.web.api;

import com.arassec.igor.core.application.ActionManager;
import com.arassec.igor.core.application.ProviderManager;
import com.arassec.igor.core.application.ServiceManager;
import com.arassec.igor.core.application.TriggerManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONObject;
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
     * Job-Mapper for simulation runs.
     */
    private final ObjectMapper objectMapper;

    /**
     * Returns all configuration parameters of a service type.
     *
     * @param type The type to get parameters for.
     *
     * @return List of parameters.
     */
    @GetMapping(value = "service/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getServiceParameters(@PathVariable("type") String type) {
        try {
            JSONObject jsonObject = new JSONObject(objectMapper.writeValueAsString(serviceManager.createInstance(type, null)));
            return jsonObject.getJSONArray("parameters").toString();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not read parameters!", e);
        }
    }

    /**
     * Returns all configuration parameters of an action type.
     *
     * @param type The type to get parameters for.
     *
     * @return List of parameters.
     */
    @GetMapping(value = "action/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getActionParameters(@PathVariable("type") String type) {
        try {
            JSONObject jsonObject = new JSONObject(objectMapper.writeValueAsString(actionManager.createInstance(type, null)));
            return jsonObject.getJSONArray("parameters").toString();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not read parameters!", e);
        }
    }

    /**
     * Returns all configuration parameters of an action type.
     *
     * @param type The type to get parameters for.
     *
     * @return List of parameters.
     */
    @GetMapping(value = "provider/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getProviderParameters(@PathVariable("type") String type) {
        try {
            JSONObject jsonObject = new JSONObject(objectMapper.writeValueAsString(providerManager.createInstance(type, null)));
            return jsonObject.getJSONArray("parameters").toString();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not read parameters!", e);
        }
    }

    /**
     * Returns all configuration parameters of an action type.
     *
     * @param type The type to get parameters for.
     *
     * @return List of parameters.
     */
    @GetMapping(value = "trigger/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getTriggerParameters(@PathVariable("type") String type) {
        try {
            JSONObject jsonObject = new JSONObject(objectMapper.writeValueAsString(triggerManager.createInstance(type, null)));
            return jsonObject.getJSONArray("parameters").toString();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not read parameters!", e);
        }
    }

}
