package com.arassec.igor.web.api.model;

import lombok.Data;

@Data
public class ServiceCategory {

    private String type;

    private String label;

    public ServiceCategory(String type, String label) {
        this.type = type;
        this.label = label;
    }

}
