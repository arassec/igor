package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.CorePluginUtils;
import com.arassec.igor.plugin.core.validation.ValidJsonObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <h2>'Add Data' Action</h2>
 *
 * <h3>Description</h3>
 * The action adds JSON data to the processed data item.
 *
 * <h3>Example</h3>
 * The following configuration of the 'Json' parameter:
 * <pre><code>
 * {
 *   "data": {
 *     "alpha": "beta",
 *     "delta": [
 *       42,
 *       23
 *     ]
 *   }
 * }
 * </code></pre>
 * <p>
 * will result in the following data item:
 * <pre><code>
 * {
 *   "data": {
 *     "alpha": "beta",
 *     "delta": [
 *       42,
 *       23
 *     ]
 *   },
 *   "meta": {
 *     "jobId": "01d11f89-1b89-4fa0-8da4-cdd75229f8b5",
 *     "timestamp": 1599580925108
 *   }
 * }
 * </code></pre>
 */
@Getter
@Setter
@IgorComponent(typeId = "add-data-action", categoryId = CoreCategory.UTIL)
public class AddDataAction extends BaseUtilAction {

    /**
     * The JSON to add to the data item. The configured JSON must not be an array!
     */
    @NotEmpty
    @ValidJsonObject
    @IgorParam(subtype = ParameterSubtype.MULTI_LINE)
    private String json;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {

        Map<String, Object> jsonData = CorePluginUtils.jsonToMap(CorePluginUtils.evaluateTemplate(data, json));

        var objectMapper = new ObjectMapper();

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
     * Thanks to <a href="ttps://stackoverflow.com/a/11459962>StackOverflow</a>!
     * @param mainNode   The main {@link JsonNode} to add the data to.
     * @param updateNode The data to add to the main node.
     *
     * @return The merged JSON.
     */
    private JsonNode merge(JsonNode mainNode, JsonNode updateNode) {

        Iterator<String> fieldNames = updateNode.fieldNames();
        while (fieldNames.hasNext()) {

            String fieldName = fieldNames.next();
            var jsonNode = mainNode.get(fieldName);
            // if field exists and is an embedded object
            if (jsonNode != null && jsonNode.isObject()) {
                merge(jsonNode, updateNode.get(fieldName));
            } else {
                if (mainNode instanceof ObjectNode node) {
                    // Overwrite field
                    JsonNode value = updateNode.get(fieldName);
                    node.replace(fieldName, value);
                }
            }

        }

        return mainNode;
    }
}
