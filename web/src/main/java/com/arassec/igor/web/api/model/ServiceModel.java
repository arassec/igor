package com.arassec.igor.web.api.model;

import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
public class ServiceModel {

    private Long id;

    private String name;

    private ServiceCategory serviceCategory;

    private ServiceType serviceType;

    private List<ParameterDefinition> parameters = new LinkedList<>();

    public ServiceModel() {
    }

    public ServiceModel(Long id, String name, ServiceCategory serviceCategory, ServiceType serviceType, List<ParameterDefinition> parameters) {
        this.id = id;
        this.name = name;
        this.serviceCategory = serviceCategory;
        this.serviceType = serviceType;
        this.parameters = parameters;
    }
}
