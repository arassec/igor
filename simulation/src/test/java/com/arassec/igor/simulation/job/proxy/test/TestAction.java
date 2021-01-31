package com.arassec.igor.simulation.job.proxy.test;


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
public class TestAction extends BaseAction {

    /**
     * A test connector.
     */
    @Getter
    @Setter
    @IgorParam
    private TestConnector testConnector;

    /**
     * Creates a new component instance.
     */
    public TestAction() {
        super("test-action-category", "test-action-type");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        return List.of();
    }

}
