package com.arassec.igor.web.controller;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.web.mapper.WebMapperKey;
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
     * Returns all configuration parameters of a connector type.
     *
     * @param type The type to get parameters for.
     *
     * @return List of parameters.
     */
    @GetMapping(value = "connector/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getConnectorParameters(@PathVariable("type") String type) {
        return serializeParameters(Connector.class, type);
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
        return serializeParameters(Action.class, type);
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
        return serializeParameters(Provider.class, type);
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
        return serializeParameters(Trigger.class, type);
    }

    /**
     * Serializes the igor parameters of the given type.
     *
     * @param typeId The type ID to get the parameters from.
     *
     * @return The parameters as JSON-String.
     */
    private Object serializeParameters(Class<? extends IgorComponent> clazz, String typeId) {
        try {
            IgorComponent igorComponent;
            if (clazz.equals(Action.class)) {
                igorComponent = igorComponentRegistry.createActionInstance(typeId, null);
            } else if (clazz.equals(Connector.class)) {
                igorComponent = igorComponentRegistry.createConnectorInstance(typeId, null);
            } else if (clazz.equals(Provider.class)) {
                igorComponent = igorComponentRegistry.createProviderInstance(typeId, null);
            } else if (clazz.equals(Trigger.class)) {
                igorComponent = igorComponentRegistry.createTriggerInstance(typeId, null);
            } else {
                throw new IllegalArgumentException("Unknown igor component type: " + clazz);
            }
            Map<String, Object> jsonMap = objectMapper.readValue(objectMapper.writeValueAsString(igorComponent),
                    new TypeReference<>() {
                    });
            return jsonMap.get(WebMapperKey.PARAMETERS.getKey());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not read parameters!", e);
        }
    }

}
