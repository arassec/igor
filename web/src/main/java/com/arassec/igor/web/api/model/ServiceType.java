package com.arassec.igor.web.api.model;

import lombok.Data;

@Data
public class ServiceType {

    private String type;

    private String label;

    public ServiceType(String type, String label) {
        this.type = type;
        this.label = label;
    }

}
