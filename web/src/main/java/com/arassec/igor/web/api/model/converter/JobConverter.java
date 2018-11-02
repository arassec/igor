package com.arassec.igor.web.api.model.converter;

import com.arassec.igor.core.application.ActionManager;
import com.arassec.igor.core.application.ProviderManager;
import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.Job;
import com.arassec.igor.core.model.Task;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.web.api.model.ActionModel;
import com.arassec.igor.web.api.model.JobModel;
import com.arassec.igor.web.api.model.TaskModel;
import com.arassec.igor.web.api.util.ParameterUtil;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component("webJobConverter")
public class JobConverter {

    @Autowired
    private ProviderManager providerManager;

    @Autowired
    private ParameterUtil parameterUtil;

    @Autowired
    private ActionManager actionManager;

    public Job convert(JSONObject jobJson) {
        Long id = jobJson.optLong("id");
        String name = jobJson.getString("name");
        if (StringUtils.isEmpty(name)) {
            throw new IllegalStateException("Name must be set!");
        }
        String trigger = jobJson.getString("trigger");
        if (StringUtils.isEmpty(trigger)) {
            throw new IllegalArgumentException("Trigger must be set!");
        }

        Job job = new Job();
        job.setId(id);
        job.setName(name);
        job.setTrigger(trigger);
        job.setDescription(jobJson.optString("description"));
        job.setActive(jobJson.getBoolean("active"));

        job.setTasks(convertTasks(jobJson.optJSONArray("tasks")));

        return job;
    }

    public JobModel convert(Job job) {
        JobModel jobModel = new JobModel();
        jobModel.setId(job.getId());
        jobModel.setName(job.getName());
        jobModel.setTrigger(job.getTrigger());
        jobModel.setDescription(job.getDescription());
        jobModel.setActive(job.isActive());

        jobModel.setTasks(convertTasks(job.getTasks()));

        return jobModel;
    }

    private List<Task> convertTasks(JSONArray taskJsons) {
        List<Task> tasks = new LinkedList<>();
        if (taskJsons != null) {
            for (int i = 0; i < taskJsons.length(); i++) {
                JSONObject taskJson = taskJsons.getJSONObject(i);
                Task task = new Task();
                task.setName(taskJson.getString("name"));
                task.setDescription(taskJson.getString("description"));
                task.setProvider(convertProvider(taskJson.getJSONObject("provider")));
                task.getActions().addAll(convertActions(taskJson.getJSONArray("actions")));
                tasks.add(task);
            }
        }
        return tasks;
    }

    private List<TaskModel> convertTasks(List<Task> tasks) {
        List<TaskModel> taskModels = new LinkedList<>();
        if (tasks != null && !tasks.isEmpty()) {
            tasks.stream().forEach(task -> {
                TaskModel taskModel = new TaskModel();
                taskModel.setName(task.getName());
                taskModel.setDescription(task.getDescription());
                taskModel.setProvider(convertProvider(task.getProvider()));
                taskModel.setActions(convertActions(task.getActions()));
                taskModels.add(taskModel);
            });
        }
        return taskModels;
    }

    private Provider convertProvider(JSONObject providerJson) {
        String type = providerJson.getString("type");
        JSONArray parameters = providerJson.getJSONArray("parameters");
        return providerManager.createProvider(type, parameterUtil.convertParameters(parameters));
    }

    private Map<String, Object> convertProvider(Provider provider) {
        Map<String, Object> providerJson = new HashMap<>();
        providerJson.put("type", provider.getClass().getName());
        providerJson.put("parameters", parameterUtil.getParameters(provider));
        return providerJson;
    }

    private List<Action> convertActions(JSONArray actionJsons) {
        List<Action> actions = new LinkedList<>();
        for (int i = 0; i < actionJsons.length(); i++) {
            JSONObject actionJson = actionJsons.getJSONObject(i);
            actions.add(actionManager.createAction(actionJson.getString("type"),
                    parameterUtil.convertParameters(actionJson.getJSONArray("parameters"))));
        }
        return actions;
    }

    private List<ActionModel> convertActions(List<Action> actions) {
        List<ActionModel> result = new LinkedList<>();
        for (Action action : actions) {
            ActionModel actionModel = new ActionModel(action.getClass().getName(), action.getClass().getAnnotation(IgorAction.class).label());
            actionModel.setParameters(parameterUtil.getParameters(action));
            result.add(actionModel);
        }
        return result;
    }

}
