package com.arassec.igor.web.api.model;

import lombok.Data;

import java.util.List;

@Data
public class ActionModel {

    private String type;

    private String label;

    private List<ParameterDefinition> parameters;

    public ActionModel(String type, String label) {
        this.type = type;
        this.label = label;
    }

}
