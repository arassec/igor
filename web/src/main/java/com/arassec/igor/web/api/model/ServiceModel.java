package com.arassec.igor.web.api.model;

import java.util.HashMap;
import java.util.Map;

public class ServiceModel {

    private String id;

    private String type;

    private Map<String, Object> parameters = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
