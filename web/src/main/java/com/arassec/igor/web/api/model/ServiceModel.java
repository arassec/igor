package com.arassec.igor.web.api.model;

import com.arassec.igor.core.application.schema.ServiceCategory;
import com.arassec.igor.core.application.schema.ServiceType;

import java.util.HashMap;
import java.util.Map;

public class ServiceModel {

    private String id;

    private ServiceCategory serviceCategory;

    private ServiceType serviceType;

    private Map<String, Object> parameters = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ServiceCategory getServiceCategory() {
        return serviceCategory;
    }

    public void setServiceCategory(ServiceCategory serviceCategory) {
        this.serviceCategory = serviceCategory;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
