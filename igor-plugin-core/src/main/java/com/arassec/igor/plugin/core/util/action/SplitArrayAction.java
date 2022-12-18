package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreCategory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <h2>'Split Array' Action</h2>
 *
 * <h3>Description</h3>
 * This action splits a JSON-Array into multiple data items. Each data item contains one element from the original array at the
 * same position the array had before.<br>
 * <p>
 * If there is no JSON-Array at the configured position, the data item will be **filtered** by this action.
 *
 * <h3>Example</h3>
 * <p>
 * An example data item processed by this action might look like this:
 * <pre><code>
 * {
 *   "data": {
 *     "input": [
 *       "a",
 *       "b",
 *       "c"
 *     ]
 *   }
 * }
 * </code></pre>
 * <p>
 * With the following configuration:<br>
 *
 * <table>
 *     <caption>Example configuration</caption>
 *     <tr>
 *         <th>Parameter</th>
 *         <th>Configuration value</th>
 *     </tr>
 *     <tr>
 *         <td>Array Selector</td>
 *         <td>{ { data.input } }</td>
 *     </tr>
 * </table>
 * <p>
 * these three data items are created by the action:
 * <pre><code>
 * {
 *   "data": {
 *     "input": "a"
 *   }
 * }
 * </code></pre>
 * <pre><code>
 * {
 *   "data": {
 *     "input": "b"
 *   }
 * }
 * </code></pre>
 * <pre><code>
 * {
 *   "data": {
 *     "input": "c"
 *   }
 * }
 * </code></pre>
 */
@Slf4j
@Getter
@Setter
@IgorComponent(typeId = "split-array-action", categoryId = CoreCategory.UTIL)
public class SplitArrayAction extends BaseUtilAction {

    /**
     * Jackson's ObjectMapper for JSON processing.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * A Mustache expression selecting a JSON-Array from the data item. The array is split and its content separated into
     * individual data items.
     */
    @NotEmpty
    @IgorParam
    private String arraySelector;

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
        var parentPointer = arrayPointer.substring(0, arrayPointer.lastIndexOf("/"));
        // e.g. /data/content/array -> array
        var leafName = arrayPointer.substring(arrayPointer.lastIndexOf("/") + 1);

        var jsonNode = objectMapper.convertValue(data, JsonNode.class);
        var array = jsonNode.at(arrayPointer);

        for (JsonNode element : array) {
            var clonedJsonNode = objectMapper.convertValue(data, JsonNode.class);
            ((ObjectNode) clonedJsonNode.at(parentPointer)).set(leafName, element);
            result.add(objectMapper.convertValue(clonedJsonNode, new TypeReference<>() {
            }));
        }

        return result;
    }

}
