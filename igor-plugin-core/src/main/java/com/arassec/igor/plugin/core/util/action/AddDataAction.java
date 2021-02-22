package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.plugin.core.CorePluginType;
import com.arassec.igor.plugin.core.CorePluginUtils;
import com.arassec.igor.plugin.core.validation.ValidJsonObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Adds data to the data item at the configured position.
 */
@Getter
@Setter
@IgorComponent
public class AddDataAction extends BaseUtilAction {

    /**
     * The JSON to add.
     */
    @NotEmpty
    @ValidJsonObject
    @IgorParam(subtype = ParameterSubtype.MULTI_LINE)
    private String json;

    /**
     * Creates a new instance.
     */
    public AddDataAction() {
        super(CorePluginType.ADD_DATA_ACTION.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {

        Map<String, Object> jsonData = CorePluginUtils.getData(CorePluginUtils.getString(data, json));

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode updateNode = objectMapper.convertValue(jsonData, JsonNode.class);
        JsonNode mainNode = objectMapper.convertValue(data, JsonNode.class);

        JsonNode result = merge(mainNode, updateNode);
        Map<String, Object> resultMap = objectMapper.convertValue(result, new TypeReference<>() {
        });

        return List.of(resultMap);
    }

    /**
     * Merges two JSON nodes.
     * <p>
     * Thanks to StackOverflow: https://stackoverflow.com/a/11459962
     *
     * @param mainNode   The main {@link JsonNode} to add the data to.
     * @param updateNode The data to add to the main node.
     *
     * @return The merged JSON.
     */
    private JsonNode merge(JsonNode mainNode, JsonNode updateNode) {

        Iterator<String> fieldNames = updateNode.fieldNames();
        while (fieldNames.hasNext()) {

            String fieldName = fieldNames.next();
            JsonNode jsonNode = mainNode.get(fieldName);
            // if field exists and is an embedded object
            if (jsonNode != null && jsonNode.isObject()) {
                merge(jsonNode, updateNode.get(fieldName));
            } else {
                if (mainNode instanceof ObjectNode) {
                    // Overwrite field
                    JsonNode value = updateNode.get(fieldName);
                    ((ObjectNode) mainNode).replace(fieldName, value);
                }
            }

        }

        return mainNode;
    }
}
