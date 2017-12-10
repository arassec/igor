package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.provider.IgorData;

/**
 * Filters the supplied data by matching a configured prefix.
 */
@IgorAction(type = "com.arassec.igor.action.misc.FilterByPrefixAction")
public class FilterByPrefixAction extends BaseAction {

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
    public boolean process(IgorData data) {
        if (isValid(data)) {
            if (!((String) data.get(dataKey)).startsWith(prefix)) {
                data.remove(dataKey);
            }
        }
        return true;
    }

}
