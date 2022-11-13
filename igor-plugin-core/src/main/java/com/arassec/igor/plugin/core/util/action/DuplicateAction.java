package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.CorePluginUtils;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Positive;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <h2>Duplicate Action</h2>
 *
 * <h3>Description</h3>
 * This action duplicates each processed data item by the configured amount.<br>
 *
 * The index of the duplicated data item is added to the result under the key 'index'.
 *
 * <h3>Examples</h3>
 * The configured amount will be the result size of the data items created by the action.<br>
 *
 * E.g. a configuration of 1 will result in the data item being passed through, a configuration of 2 will result in a duplication
 * of the processed data item, and so on...
 */
@Getter
@Setter
@IgorComponent(typeId = "duplicate-action", categoryId = CoreCategory.UTIL)
public class DuplicateAction extends BaseAction {

    /**
     * The amount of duplicate data items that should be created by this action.
     */
    @Positive
    @IgorParam
    private int amount = 2;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (var i = 0; i < amount; i++) {
            Map<String, Object> clone = CorePluginUtils.clone(data);
            clone.put("index", i);
            result.add(clone);
        }
        return result;
    }

}
