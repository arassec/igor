package com.arassec.igor.persistence.test;

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
public class TestAction extends BaseAction {

    /**
     * The category ID.
     */
    public static final String CATEGORY_ID = "action-category-id";

    /**
     * The type ID.
     */
    public static final String TYPE_ID = "action-type-id";

    /**
     * A connector as parameter.
     */
    @IgorParam
    private Connector testConnector;

    /**
     * Creates a new component instance.
     */
    public TestAction() {
        super(CATEGORY_ID, TYPE_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        return null;
    }

}
