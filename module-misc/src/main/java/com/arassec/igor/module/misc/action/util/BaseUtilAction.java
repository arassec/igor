package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.action.BaseAction;

/**
 * Base class for actions that don't really fit into any other category.
 */
public abstract class BaseUtilAction extends BaseAction {

    /**
     * The (default) time format to use for parsing data timestamps.
     */
    String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCategoryId() {
        return "c2ff4c41-fae4-4b9b-98a8-1d5d1ce6e0be";
    }

}
