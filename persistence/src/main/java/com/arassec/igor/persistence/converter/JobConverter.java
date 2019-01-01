package com.arassec.igor.persistence.converter;

import com.arassec.igor.core.application.factory.ActionFactory;
import com.arassec.igor.core.application.factory.ProviderFactory;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.Task;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.repository.ServiceRepository;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Converts {@link Job}s into their JSON representation and vice versa.
 */
@Slf4j
@Component
public class JobConverter {

    /**
     * Suffix for services. TODO: There must be a better way to indicate an IgorService.
     */
    private static final String SERVICE_SUFFIX = "_IgorService";

    /**
     * Factory for {@link Action}s.
     */
    @Autowired
    private ActionFactory actionFactory;

    /**
     * Factory for {@link Provider}s.
     */
    @Autowired
    private ProviderFactory providerFactory;

    /**
     * Factory for {@link Service}s.
     */
    @Autowired
    private ServiceRepository serviceRepository;

    /**
     * Converts the supplied job into its JSON representation.
     *
     * @param job The job to convert.
     * @return The job's JSON as string.
     */
    public String convert(Job job) {
        JSONObject jobJson = new JSONObject();
        jobJson.put(JsonKeys.NAME, job.getName());
        jobJson.put(JsonKeys.TRIGGER, job.getTrigger());
        jobJson.put(JsonKeys.DESCRIPTION, job.getDescription());
        jobJson.put(JsonKeys.ACTIVE, job.isActive());

        JSONArray tasksJson = new JSONArray();
        job.getTasks().stream().map(task -> convertTaskToJson(task)).forEach(jsonObject -> tasksJson.put(jsonObject));
        jobJson.put(JsonKeys.TASKS, tasksJson);
        return jobJson.toString();
    }

    /**
     * Converts the JSON-string into a {@link Job} instance.
     *
     * @param jobString The job as JSON-string.
     * @return The newly created Job.
     */
    public Job convert(String jobString) {
        JSONObject jobJson = new JSONObject(jobString);

        Job job = new Job();
        job.setName(jobJson.getString(JsonKeys.NAME));
        job.setTrigger(jobJson.getString(JsonKeys.TRIGGER));
        job.setDescription(jobJson.optString(JsonKeys.DESCRIPTION));
        job.setActive(jobJson.optBoolean(JsonKeys.ACTIVE));

        JSONArray tasksArray = jobJson.getJSONArray(JsonKeys.TASKS);
        for (int i = 0; i < tasksArray.length(); i++) {
            try {
                job.getTasks().add(convertJsonToTask(tasksArray.getJSONObject(i)));
            } catch (ConversionException e) {
                log.error("Error during conversion of job {}: {}", job.getId(), e.getMessage());
                return null;
            }
        }

        return job;
    }

    /**
     * Converts the provided JSON into a {@link Task}.
     *
     * @param taskJson The task in JSON form.
     * @return A newly created Task instance.
     * @throws ConversionException In case the JSONObject could not be converted.
     */
    private Task convertJsonToTask(JSONObject taskJson) throws ConversionException {
        Task task = new Task();
        String taskName = taskJson.getString(JsonKeys.NAME);
        if (taskName == null || taskName.isEmpty()) {
            throw new ConversionException("Task name is empty!");
        }
        task.setName(taskName);
        task.setDescription(taskJson.optString(JsonKeys.DESCRIPTION));
        Provider provider = convertJsonToProvider(taskJson.getJSONObject(JsonKeys.PROVIDER));
        if (provider == null) {
            throw new ConversionException("Provider is null!");
        }
        task.setProvider(provider);

        JSONArray actionsArray = taskJson.getJSONArray(JsonKeys.ACTIONS);
        for (int i = 0; i < actionsArray.length(); i++) {
            task.getActions().add(convertJsonToAction(actionsArray.getJSONObject(i)));
        }
        task.getActions().removeIf(Objects::isNull);

        return task;
    }

    /**
     * Converts the provided JSON into a {@link Provider}.
     *
     * @param providerJson The JSON with provider data.
     * @return A newly created Provider instance.
     */
    private Provider convertJsonToProvider(JSONObject providerJson) {
        Map<String, Object> parameters = convertJsonToParameters(providerJson.getJSONObject(JsonKeys.PARAMETERS));
        return providerFactory.createInstance(providerJson.getString(JsonKeys.TYPE), parameters);
    }

    /**
     * Converts the provided JSON into an {@link Action}.
     *
     * @param actionJson The JSON with action data.
     * @return A newly created Action instance.
     */
    private Action convertJsonToAction(JSONObject actionJson) {
        Map<String, Object> parameters = convertJsonToParameters(actionJson.getJSONObject(JsonKeys.PARAMETERS));
        return actionFactory.createInstance(actionJson.getString(JsonKeys.TYPE), parameters);
    }

    /**
     * Converts the provided JSON into a parameter map.
     *
     * @param parameters The parameters in JSON form.
     * @return A map containing the parameter keys and values.
     */
    private Map<String, Object> convertJsonToParameters(JSONObject parameters) {
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < parameters.length(); i++) {
            String parameterName = parameters.names().getString(i);
            if (parameterName.endsWith(SERVICE_SUFFIX)) {
                params.put(parameterName.replace(SERVICE_SUFFIX, ""), serviceRepository.findById(parameters.getLong(parameterName)));
            } else {
                params.put(parameterName, parameters.get(parameterName));
            }
        }
        return params;
    }

    /**
     * Converts a {@link Task} into its JSON representation.
     *
     * @param task The task to convert.
     * @return A {@link JSONObject} representing the task.
     */
    private JSONObject convertTaskToJson(Task task) {
        JSONObject taskJson = new JSONObject();
        taskJson.put(JsonKeys.NAME, task.getName());
        taskJson.put(JsonKeys.DESCRIPTION, task.getDescription());
        taskJson.put(JsonKeys.PROVIDER, convertProviderToJson(task.getProvider()));
        taskJson.put(JsonKeys.ACTIONS, task.getActions().stream().map(action -> convertActionToJson(action)).collect(Collectors.toList()));
        return taskJson;
    }

    /**
     * Converts a {@link Provider} into its JSON representation.
     *
     * @param provider The provider to convert.
     * @return A {@link JSONObject} with the provider's data.
     */
    private JSONObject convertProviderToJson(Provider provider) {
        JSONObject providerJson = new JSONObject();
        providerJson.put(JsonKeys.TYPE, provider.getClass().getName());
        providerJson.put(JsonKeys.PARAMETERS, convertParametersToJson(providerFactory.getParameters(provider)));
        return providerJson;
    }

    /**
     * Converts an {@link Action} into a {@link JSONObject}.
     *
     * @param action The action to convert.
     * @return A JSONObject with the action's data.
     */
    private JSONObject convertActionToJson(Action action) {
        JSONObject actionJson = new JSONObject();
        actionJson.put(JsonKeys.TYPE, action.getClass().getName());
        actionJson.put(JsonKeys.PARAMETERS, convertParametersToJson(actionFactory.getParameters(action)));
        return actionJson;
    }

    /**
     * Converts a map with parameter keys and values into a {@link JSONObject}.
     *
     * @param parameters The parameters to convert.
     * @return A JSON containing the parameters.
     */
    private JSONObject convertParametersToJson(Map<String, Object> parameters) {
        JSONObject params = new JSONObject();
        for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
            if (parameter.getValue() instanceof Service) {
                Service service = (Service) parameter.getValue();
                params.put(parameter.getKey() + SERVICE_SUFFIX, service.getId());
            } else {
                params.put(parameter.getKey(), parameter.getValue());
            }
        }
        return params;
    }

}
