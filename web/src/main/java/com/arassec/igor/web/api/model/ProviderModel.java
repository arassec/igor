package com.arassec.igor.web.api.model;

import lombok.Data;

@Data
public class ProviderModel {

    private String type;

    private String label;

    public ProviderModel(String type, String label) {
        this.type = type;
        this.label = label;
    }
}
