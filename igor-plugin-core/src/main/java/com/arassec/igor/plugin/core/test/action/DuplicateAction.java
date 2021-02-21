package com.arassec.igor.plugin.core.test.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CorePluginType;
import com.arassec.igor.plugin.core.CoreUtils;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Positive;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Duplicates data items to the configured amount.
 */
@Getter
@Setter
@IgorComponent
public class DuplicateAction extends BaseTestAction {

    /**
     * The number of duplications to create.
     */
    @Positive
    @IgorParam(defaultValue = "2")
    private int amount;

    /**
     * Creates a new component instance.
     */
    protected DuplicateAction() {
        super(CorePluginType.DUPLICATE_ACTION.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (int i = 0; i < amount; i++) {
            Map<String, Object> clone = CoreUtils.clone(data);
            clone.put("index", i);
            result.add(clone);
        }
        return result;
    }

}
