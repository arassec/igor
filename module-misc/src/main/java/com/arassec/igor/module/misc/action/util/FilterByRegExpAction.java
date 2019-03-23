package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.provider.IgorData;

/**
 * Filters the supplied data by a regular expression.
 */
@IgorAction(label = "Filter by regular expression")
public class FilterByRegExpAction extends BaseUtilAction {

    /**
     * The Regular expression to filter the input with.
     */
    @IgorParam
    private String expression;

    /**
     * Matches the provided data against the configured regular expression and filters it, if it doesn't match.
     *
     * @param data The data the action will work with.
     * @return {@code true}, if the value under the configured {@link BaseAction#dataKey} matches the regular expresion,
     * {@code false} otherwise.
     */
    @Override
    public boolean process(IgorData data) {
        if (isValid(data)) {
            if (!((String) data.get(dataKey)).matches(expression)) {
                return false;
            }
        }
        return true;
    }

}
