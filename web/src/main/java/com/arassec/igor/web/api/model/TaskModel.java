package com.arassec.igor.web.api.model;

import com.arassec.igor.core.model.provider.Provider;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class TaskModel {

    private String name;

    private String description;

    private Provider provider;

    private List<ActionModel> actions = new LinkedList<>();

}
