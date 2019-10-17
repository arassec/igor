package com.arassec.igor.web.controller;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.web.mapper.WebMapperKeyAware;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST-Controller for parameter requests.
 */
@RestController()
@RequestMapping("/api/parameters")
@RequiredArgsConstructor
public class ParametersRestController {

    /**
     * The component registry.
     */
    private final IgorComponentRegistry igorComponentRegistry;

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
    public Object getServiceParameters(@PathVariable("type") String type) {
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
    public Object getActionParameters(@PathVariable("type") String type) {
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
    public Object getProviderParameters(@PathVariable("type") String type) {
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
    public Object getTriggerParameters(@PathVariable("type") String type) {
        return serializeParameters(type);
    }

    /**
     * Serializes the igor parameters of the given type.
     *
     * @param typeId The type ID to get the parameters from.
     *
     * @return The parameters as JSON-String.
     */
    private Object serializeParameters(String typeId) {
        try {
            IgorComponent igorComponent = igorComponentRegistry.getClass(typeId).orElseThrow(() -> new IllegalArgumentException("Unknown type ID: " + typeId));
            Map<String, Object> jsonMap = objectMapper.readValue(objectMapper.writeValueAsString(igorComponent),
                    new TypeReference<>() {
                    });
            return jsonMap.get(WebMapperKeyAware.PARAMETERS);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not read parameters!", e);
        }
    }

}
