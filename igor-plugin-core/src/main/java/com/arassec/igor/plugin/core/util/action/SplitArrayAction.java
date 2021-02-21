package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CorePluginType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotEmpty;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Action that converts a JSON-Array into multiple data items with a single JSON-Object containing one of the old values from the
 * array.
 */
@Slf4j
@Getter
@Setter
@IgorComponent
public class SplitArrayAction extends BaseUtilAction {

    /**
     * Jackson's ObjectMapper for JSON processing.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * The JSON-Path query to the array element.
     */
    @NotEmpty
    @IgorParam
    private String arraySelector;

    /**
     * Creates a new component instance.
     */
    public SplitArrayAction() {
        super(CorePluginType.SPLIT_ARRAY_ACTION.getId());
    }

    /**
     * Converts a JSON-Array at the configured position and returns multiple data items from it. Each data item contains only a
     * single value from the array at the old array's position.
     *
     * @param data         The data the action will work with.
     * @param jobExecution The job execution log.
     *
     * @return Data items for each array element.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {

        List<Map<String, Object>> result = new LinkedList<>();

        // e.g. {{data.content.array}} -> /data/content/array
        String arrayPointer = arraySelector.replace("{{", "/").replace(".", "/").replace("}}", "");
        // e.g. /data/content/array -> /data/content
        String parentPointer = arrayPointer.substring(0, arrayPointer.lastIndexOf("/"));
        // e.g. /data/content/array -> array
        String leafName = arrayPointer.substring(arrayPointer.lastIndexOf("/") + 1);

        JsonNode jsonNode = objectMapper.convertValue(data, JsonNode.class);
        JsonNode array = jsonNode.at(arrayPointer);

        for (JsonNode element : array) {
            JsonNode clonedJsonNode = objectMapper.convertValue(data, JsonNode.class);
            ((ObjectNode) clonedJsonNode.at(parentPointer)).set(leafName, element);
            result.add(objectMapper.convertValue(clonedJsonNode, new TypeReference<>() {
            }));
        }

        return result;
    }

}
