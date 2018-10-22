package com.arassec.igor.web.api.model;

import lombok.Data;

@Data
public class ParameterDefinition {

    private String name;

    private String type;

    private Object value;

    private boolean optional;

    private boolean secured;

    private boolean service;

    private String serviceName;

    public ParameterDefinition(String name, String type, Object value, boolean optional, boolean secured, boolean service) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.optional = optional;
        this.secured = secured;
        this.service = service;
    }

}
