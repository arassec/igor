package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;

import java.util.List;
import java.util.Map;

/**
 * Filters the supplied data by matching a configured prefix.
 */
@IgorAction(label = "Filter by prefix")
public class FilterByPrefixAction extends BaseUtilAction {

    /**
     * The prefix to filter the input for.
     */
    @IgorParam
    private String prefix;

    /**
     * Selects the provided data by matching the configured prefix.
     *
     * @param data The data to processData.
     * @return The original data, if it matches the configured prefix.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, boolean isDryRun) {
        if (isValid(data)) {
            if (!((String) data.get(dataKey)).startsWith(prefix)) {
                return null;
            }
        }
        return List.of(data);
    }

}
