package com.arassec.igor.web.api.model;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class JobModel {

    private Long id;

    private String name;

    private String trigger;

    private String description;

    private boolean active;

    private List<TaskModel> tasks = new LinkedList<>();

    public JobModel() {
    }

    public JobModel(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
