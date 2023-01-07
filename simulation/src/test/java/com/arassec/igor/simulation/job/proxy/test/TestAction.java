package com.arassec.igor.simulation.job.proxy.test;


import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * An action for testing.
 */
@IgorComponent(categoryId = "test-action-category", typeId = "test-action-type")
public class TestAction extends BaseAction {

    /**
     * A test connector.
     */
    @Getter
    @Setter
    @IgorParam
    private TestConnector testConnector;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        return List.of();
    }

}
