package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.model.job.Task;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Converts {@link com.arassec.igor.core.model.job.Task}s into their JSON representation and vice versa.
 */
@Slf4j
@Component
public class JsonTaskConverter {

    @Autowired
    private JsonProviderConverter jsonProviderConverter;

    @Autowired
    private JsonActionConverter jsonActionConverter;

    /**
     * Converts a {@link Task} into its JSON representation.
     *
     * @param task The task to convert.
     * @return A {@link JSONObject} representing the task.
     */
    public JSONObject convert(Task task, boolean applySecurity) {
        JSONObject taskJson = new JSONObject();
        taskJson.put(JsonKeys.NAME, task.getName());
        taskJson.put(JsonKeys.DESCRIPTION, task.getDescription());
        taskJson.put(JsonKeys.PROVIDER, jsonProviderConverter.convert(task.getProvider(), applySecurity));
        JSONArray actionJsons = new JSONArray();
        task.getActions().stream().map(action -> jsonActionConverter.convert(action, applySecurity))
                .forEach(actionJson -> actionJsons.put(actionJson));
        taskJson.put(JsonKeys.ACTIONS, actionJsons);
        return taskJson;
    }

    public Task convert(JSONObject taskJson, boolean applySecurity) {
        Task result = new Task();
        result.setName(taskJson.getString(JsonKeys.NAME));
        result.setDescription(taskJson.optString(JsonKeys.DESCRIPTION));
        result.setProvider(jsonProviderConverter.convert(taskJson.getJSONObject(JsonKeys.PROVIDER), applySecurity));
        JSONArray actionJsons = taskJson.optJSONArray(JsonKeys.ACTIONS);
        for (int i = 0; i < actionJsons.length(); i++) {
            actionJsons.put(jsonActionConverter.convert(actionJsons.getJSONObject(i), applySecurity));
        }
        return result;
    }

}
