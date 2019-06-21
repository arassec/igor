package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.model.job.Job;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Converts {@link Job}s into their JSON representation and vice versa.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonJobConverter {

    /**
     * The JSON task converter.
     */
    private final JsonTaskConverter jsonTaskConverter;

    /**
     * The JSON trigger converter.
     */
    private final JsonTriggerConverter jsonTriggerConverter;

    /**
     * Converts the supplied job into its JSON representation.
     *
     * @param job           The job to convert.
     * @param applySecurity Set to {@code true}, to encrypt secured parameters.
     * @param addVolatile   Set to {@code true} to add properties that only exist through annotations or could otherwise
     *                      be obtained, but can be added for convenience.
     * @return The job's JSON as string.
     */
    public JSONObject convert(Job job, boolean applySecurity, boolean addVolatile) {
        JSONObject jobJson = new JSONObject();
        jobJson.put(JsonKeys.ID, job.getId());
        jobJson.put(JsonKeys.NAME, job.getName());
        jobJson.put(JsonKeys.TRIGGER, jsonTriggerConverter.convert(job.getTrigger(), applySecurity, addVolatile));
        jobJson.put(JsonKeys.DESCRIPTION, job.getDescription());
        jobJson.put(JsonKeys.EXECUTION_HISTORY_LIMIT, job.getExecutionHistoryLimit());
        jobJson.put(JsonKeys.ACTIVE, job.isActive());

        JSONArray tasksJson = new JSONArray();
        job.getTasks().stream().map(task -> jsonTaskConverter.convert(task, applySecurity, addVolatile)).forEach(tasksJson::put);
        jobJson.put(JsonKeys.TASKS, tasksJson);

        return jobJson;
    }

    /**
     * Converts jobs from their JSON representation.
     *
     * @param jobJson       The job in JSON form.
     * @param id            Optional ID of the job. Used, if the job's JSON doesn't contain an ID.
     * @param applySecurity Set to {@code true}, to decrypt secured parameters.
     * @return A newly created {@link Job} instance.
     */
    public Job convert(JSONObject jobJson, Long id, boolean applySecurity) {
        Job job = new Job();
        job.setId(jobJson.optLong(JsonKeys.ID, -1));
        if (job.getId() == -1) {
            if (id != null) {
                job.setId(id);
            } else {
                job.setId(null);
            }
        }
        job.setName(jobJson.getString(JsonKeys.NAME));
        job.setTrigger(jsonTriggerConverter.convert(jobJson.getJSONObject(JsonKeys.TRIGGER), applySecurity));
        job.setDescription(jobJson.optString(JsonKeys.DESCRIPTION));
        job.setExecutionHistoryLimit(jobJson.optInt(JsonKeys.EXECUTION_HISTORY_LIMIT));
        job.setActive(jobJson.getBoolean(JsonKeys.ACTIVE));

        JSONArray taskJsons = jobJson.optJSONArray(JsonKeys.TASKS);
        if (taskJsons != null) {
            for (int i = 0; i < taskJsons.length(); i++) {
                job.getTasks().add(jsonTaskConverter.convert(taskJsons.getJSONObject(i), applySecurity));
            }
        }

        return job;
    }

}
