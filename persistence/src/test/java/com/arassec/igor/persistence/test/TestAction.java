package com.arassec.igor.persistence.test;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.job.execution.JobExecution;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * An action just for testing.
 */
@Getter
@Setter
@IgorComponent(typeId = "action-type-id", categoryId = "action-category-id")
public class TestAction extends BaseAction {

    /**
     * A connector as parameter.
     */
    @IgorParam
    private Connector testConnector;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        return null;
    }

}
