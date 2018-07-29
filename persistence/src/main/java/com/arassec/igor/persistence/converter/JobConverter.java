package com.arassec.igor.persistence.converter;

import com.arassec.igor.core.application.factory.ActionFactory;
import com.arassec.igor.core.application.factory.ProviderFactory;
import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorProvider;
import com.arassec.igor.core.model.Job;
import com.arassec.igor.core.model.Task;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.repository.ServiceRepository;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Converts jobs into their JSON representation and vice versa.
 */
@Component
public class JobConverter {

    private static final Logger LOG = LoggerFactory.getLogger(JobConverter.class);

    private static final String SERVICE = "Service";

    @Autowired
    private ActionFactory actionFactory;

    @Autowired
    private ProviderFactory providerFactory;

    @Autowired
    private ServiceRepository serviceRepository;

    public String convert(Job job) {
        JSONObject jobJson = new JSONObject();
        jobJson.put(JsonKeys.ID, job.getId());
        jobJson.put(JsonKeys.TRIGGER, job.getTrigger());
        jobJson.put(JsonKeys.DESCRIPTION, job.getDescription());

        JSONArray tasksJson = new JSONArray();
        job.getTasks().stream().map(task -> convertTaskToJson(task)).forEach(jsonObject -> tasksJson.put(jsonObject));
        jobJson.put(JsonKeys.TASKS, tasksJson);
        return jobJson.toString();
    }

    public Job convert(String jobString) {
        JSONObject jobJson = new JSONObject(jobString);

        Job job = new Job();
        job.setId(jobJson.getString(JsonKeys.ID));
        job.setTrigger(jobJson.getString(JsonKeys.TRIGGER));
        job.setDescription(jobJson.optString(JsonKeys.DESCRIPTION));

        JSONArray tasksArray = jobJson.getJSONArray(JsonKeys.TASKS);
        for (int i = 0; i < tasksArray.length(); i++) {
            try {
                job.getTasks().add(convertJsonToTask(tasksArray.getJSONObject(i)));
            } catch (ConversionException e) {
                LOG.error("Error during conversion of job {}: {}", job.getId(), e.getMessage());
                return null;
            }
        }

        return job;
    }

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

    private Provider convertJsonToProvider(JSONObject providerJson) {
        Map<String, Object> parameters = convertJsonToParameters(providerJson.getJSONObject(JsonKeys.PARAMETERS));
        return providerFactory.createInstance(providerJson.getString(JsonKeys.TYPE), parameters);
    }

    private Action convertJsonToAction(JSONObject actionJson) {
        Map<String, Object> parameters = convertJsonToParameters(actionJson.getJSONObject(JsonKeys.PARAMETERS));
        return actionFactory.createInstance(actionJson.getString(JsonKeys.TYPE), parameters);
    }

    private Map<String, Object> convertJsonToParameters(JSONObject parameters) {
        Map<String, Object> params = new HashMap<>();
        for (int i=0; i < parameters.length(); i++) {
            String parameterName = parameters.names().getString(i);
            // TODO: There has to be a better way to identify a service!
            if (parameterName.equalsIgnoreCase(SERVICE) || parameterName.endsWith(SERVICE)) {
                params.put(parameterName, serviceRepository.findById(parameters.getString(parameterName)));
            } else {
                params.put(parameterName, parameters.get(parameterName));
            }
        }
        return params;
    }

    private JSONObject convertTaskToJson(Task task) {
        JSONObject taskJson = new JSONObject();
        taskJson.put(JsonKeys.NAME, task.getName());
        taskJson.put(JsonKeys.DESCRIPTION, task.getDescription());
        taskJson.put(JsonKeys.PROVIDER, convertProviderToJson(task.getProvider()));
        taskJson.put(JsonKeys.ACTIONS, task.getActions().stream().map(action -> convertActionToJson(action)).collect(Collectors.toList()));
        return taskJson;
    }

    private JSONObject convertProviderToJson(Provider provider) {
        JSONObject providerJson = new JSONObject();
        providerJson.put(JsonKeys.TYPE, provider.getClass().getName());
        providerJson.put(JsonKeys.PARAMETERS, convertParametersToJson(providerFactory.getParameters(provider)));
        return providerJson;
    }

    private JSONObject convertActionToJson(Action action) {
        JSONObject actionJson = new JSONObject();
        actionJson.put(JsonKeys.TYPE, action.getClass().getName());
        actionJson.put(JsonKeys.PARAMETERS, convertParametersToJson(actionFactory.getParameters(action)));
        return actionJson;
    }

    private JSONObject convertParametersToJson(Map<String, Object> parameters) {
        JSONObject params = new JSONObject();
        for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
            if (parameter.getValue() instanceof Service) {
                Service service = (Service) parameter.getValue();
                params.put(parameter.getKey(), service.getId());
            } else {
                params.put(parameter.getKey(), parameter.getValue());
            }
        }
        return params;
    }

}
