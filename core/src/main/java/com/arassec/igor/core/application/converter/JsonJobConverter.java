package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.model.job.Job;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Converts {@link Job}s into their JSON representation and vice versa.
 */
@Slf4j
@Component
public class JsonJobConverter {

    @Autowired
    private JsonTaskConverter jsonTaskConverter;

    /**
     * Converts the supplied job into its JSON representation.
     *
     * @param job The job to convert.
     * @return The job's JSON as string.
     */
    public JSONObject convert(Job job, boolean applySecurity) {
        JSONObject jobJson = new JSONObject();
        jobJson.put(JsonKeys.ID, job.getId());
        jobJson.put(JsonKeys.NAME, job.getName());
        jobJson.put(JsonKeys.TRIGGER, job.getTrigger());
        jobJson.put(JsonKeys.DESCRIPTION, job.getDescription());
        jobJson.put(JsonKeys.ACTIVE, job.isActive());

        JSONArray tasksJson = new JSONArray();
        job.getTasks().stream().map(task -> jsonTaskConverter.convert(task, applySecurity))
                .forEach(jsonObject -> tasksJson.put(jsonObject));
        jobJson.put(JsonKeys.TASKS, tasksJson);

        return jobJson;
    }

    public Job convert(JSONObject jobJson, boolean applySecurity) {
        Job job = new Job();
        job.setId(jobJson.optLong(JsonKeys.ID));
        job.setName(jobJson.getString(JsonKeys.NAME));
        job.setTrigger(jobJson.getString(JsonKeys.TRIGGER));
        job.setDescription(jobJson.getString(jobJson.optString(JsonKeys.DESCRIPTION)));
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
