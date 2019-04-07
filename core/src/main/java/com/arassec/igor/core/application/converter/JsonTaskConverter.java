package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.model.job.Task;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Converts {@link com.arassec.igor.core.model.job.Task}s into their JSON representation and vice versa.
 */
@Slf4j
@Component
public class JsonTaskConverter {

    /**
     * Converter for providers.
     */
    @Autowired
    private JsonProviderConverter jsonProviderConverter;

    /**
     * Converter for actions.
     */
    @Autowired
    private JsonActionConverter jsonActionConverter;

    /**
     * Converts a {@link Task} into its JSON representation.
     *
     * @param task          The task to convert.
     * @param applySecurity Set to {@code true} to encrypt secured properties. If set to {@code false}, secured
     *                      properties will be kept in cleartext form.
     * @param addVolatile   Set to {@code true} to add properties that only exist through annotations or could otherwise
     *                      be obtained, but can be added for convenience.
     * @return A {@link JSONObject} representing the task.
     */
    public JSONObject convert(Task task, boolean applySecurity, boolean addVolatile) {
        JSONObject taskJson = new JSONObject();
        if (task.getId() == null || task.getId().trim().isEmpty()) {
            task.setId(UUID.randomUUID().toString());
        }
        taskJson.put(JsonKeys.ID, task.getId());
        taskJson.put(JsonKeys.NAME, task.getName());
        taskJson.put(JsonKeys.DESCRIPTION, task.getDescription());
        taskJson.put(JsonKeys.ACTIVE, task.isActive());
        taskJson.put(JsonKeys.PROVIDER, jsonProviderConverter.convert(task.getProvider(), applySecurity, addVolatile));
        JSONArray actionJsons = new JSONArray();
        task.getActions().stream().map(action -> jsonActionConverter.convert(action, applySecurity, addVolatile))
                .forEach(actionJson -> actionJsons.put(actionJson));
        taskJson.put(JsonKeys.ACTIONS, actionJsons);
        return taskJson;
    }

    /**
     * Converts a {@link Task} in JSON form into an object instance.
     *
     * @param taskJson      The task in JSON form.
     * @param applySecurity Set to {@link true}, to decrypt secured properties.
     * @return A newly created Task instance.
     */
    public Task convert(JSONObject taskJson, boolean applySecurity) {
        String id = taskJson.optString(JsonKeys.ID);
        if (id == null || id.trim().isEmpty()) {
            id = UUID.randomUUID().toString();
        }
        Task result = new Task(id);
        result.setName(taskJson.getString(JsonKeys.NAME));
        result.setDescription(taskJson.optString(JsonKeys.DESCRIPTION));
        result.setActive(taskJson.optBoolean(JsonKeys.ACTIVE, true));
        result.setProvider(jsonProviderConverter.convert(taskJson.getJSONObject(JsonKeys.PROVIDER), applySecurity));
        JSONArray actionJsons = taskJson.optJSONArray(JsonKeys.ACTIONS);
        for (int i = 0; i < actionJsons.length(); i++) {
            result.getActions().add(jsonActionConverter.convert(actionJsons.getJSONObject(i), applySecurity));
        }
        return result;
    }

}
