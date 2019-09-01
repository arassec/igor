package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.model.job.Task;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Converts {@link com.arassec.igor.core.model.job.Task}s into their JSON representation and vice versa.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonTaskConverter {

    /**
     * Converter for providers.
     */
    private final JsonProviderConverter jsonProviderConverter;

    /**
     * Converter for actions.
     */
    private final JsonActionConverter jsonActionConverter;

    /**
     * Converts a {@link Task} into its JSON representation.
     *
     * @param task   The task to convert.
     * @param config The converter configuration.
     *
     * @return A {@link JSONObject} representing the task.
     */
    public JSONObject convert(Task task, ConverterConfig config) {
        JSONObject taskJson = new JSONObject();
        if (task.getId() == null || task.getId().trim().isEmpty()) {
            task.setId(UUID.randomUUID().toString());
        }
        taskJson.put(JsonKeys.ID, task.getId());
        taskJson.put(JsonKeys.NAME, task.getName());
        taskJson.put(JsonKeys.DESCRIPTION, task.getDescription());
        taskJson.put(JsonKeys.ACTIVE, task.isActive());
        taskJson.put(JsonKeys.SIMULATION_LIMIT, task.getSimulationLimit());
        taskJson.put(JsonKeys.PROVIDER, jsonProviderConverter.convert(task.getProvider(), config));
        JSONArray actionJsons = new JSONArray();
        task.getActions().stream().map(action -> jsonActionConverter.convert(action, config))
                .forEach(actionJsons::put);
        taskJson.put(JsonKeys.ACTIONS, actionJsons);
        return taskJson;
    }

    /**
     * Converts a {@link Task} in JSON form into an object instance.
     *
     * @param taskJson The task in JSON form.
     * @param config   The converter configuration.
     *
     * @return A newly created Task instance.
     */
    public Task convert(JSONObject taskJson, ConverterConfig config) {
        String id = taskJson.optString(JsonKeys.ID);
        if (id == null || id.trim().isEmpty()) {
            id = UUID.randomUUID().toString();
        }
        Task result = new Task(id);
        result.setName(taskJson.getString(JsonKeys.NAME));
        result.setDescription(taskJson.optString(JsonKeys.DESCRIPTION));
        result.setActive(taskJson.optBoolean(JsonKeys.ACTIVE, true));
        result.setSimulationLimit(taskJson.optInt(JsonKeys.SIMULATION_LIMIT, 25));
        result.setProvider(jsonProviderConverter.convert(taskJson.getJSONObject(JsonKeys.PROVIDER), config));
        JSONArray actionJsons = taskJson.optJSONArray(JsonKeys.ACTIONS);
        for (int i = 0; i < actionJsons.length(); i++) {
            result.getActions().add(jsonActionConverter.convert(actionJsons.getJSONObject(i), config));
        }
        if (config.getSimulationDataCollector() != null) {
            config.getSimulationDataCollector().getProviderProxies()
                    .get(config.getSimulationDataCollector().getCurIndex()).setSimulationLimit(result.getSimulationLimit());
            config.getSimulationDataCollector().setCurIndex(config.getSimulationDataCollector().getCurIndex() + 1);
        }
        return result;
    }

}
