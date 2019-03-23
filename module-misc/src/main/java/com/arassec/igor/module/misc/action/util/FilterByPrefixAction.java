package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.provider.IgorData;

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
    public boolean process(IgorData data) {
        if (isValid(data)) {
            if (!((String) data.get(dataKey)).startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }

}
