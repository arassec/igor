package com.arassec.igor.web.api.model;

import lombok.Data;

@Data
public class ParameterDefinition {

    private String name;

    private String type;

    private Object value;

    private boolean optional;

    private boolean secured;

    public ParameterDefinition(String name, String type, Object value, boolean optional, boolean secured) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.optional = optional;
        this.secured = secured;
    }

}
