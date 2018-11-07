package com.arassec.igor.core.model.action.misc;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.provider.IgorData;

@IgorAction(label = "Filter by regular expression")
public class FilterByRegExpAction extends BaseAction {

    /**
     * The Regular expression to filter the input with.
     */
    @IgorParam
    private String expression;

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
