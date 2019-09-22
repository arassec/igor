package com.arassec.igor.web.api;

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
        return serializeParameters(type);
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
        return serializeParameters(type);
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
        return serializeParameters(type);
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
        return serializeParameters(type);
    }

    /**
     * Serializes the igor parameters of the given type.
     *
     * @param type The type to get the parameters from.
     *
     * @return The parameters as JSON-String.
     */
    private String serializeParameters(String type) {
        try {
            JSONObject jsonObject = new JSONObject(objectMapper.writeValueAsString(Class.forName(type).getConstructor().newInstance()));
            return jsonObject.getJSONArray("parameters").toString();
        } catch (JsonProcessingException | ReflectiveOperationException e) {
            throw new IllegalArgumentException("Could not read parameters!", e);
        }
    }

}
