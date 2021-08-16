package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.core.model.action.BaseAction;

/**
 * Base class for actions that don't really fit into any other category.
 */
public abstract class BaseUtilAction extends BaseAction {

    /**
     * The default time unit to use in actions as default parameter value.
     */
    protected static final String DEFAULT_TIME_UNIT = "DAYS";

    /**
     * The default time format to use for parsing data timestamps.
     */
    protected static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

}
