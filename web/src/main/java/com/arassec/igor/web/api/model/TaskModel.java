package com.arassec.igor.web.api.model;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
public class TaskModel {

    private String name;

    private String description;

    private Map<String, Object> provider;

    private List<ActionModel> actions = new LinkedList<>();

}
