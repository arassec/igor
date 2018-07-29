package com.arassec.igor.core.application.schema;

public class ParameterDefinition {

    private String name;

    private String type;

    private boolean optional;

    private boolean secured;

    public ParameterDefinition(String name, String type, boolean optional, boolean secured) {
        this.name = name;
        this.type = type;
        this.optional = optional;
        this.secured = secured;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public boolean isSecured() {
        return secured;
    }

    public void setSecured(boolean secured) {
        this.secured = secured;
    }
}
