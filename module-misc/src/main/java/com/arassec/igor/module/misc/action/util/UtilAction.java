package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.IgorCategory;
import com.arassec.igor.core.model.action.Action;

/**
 * Defines methods for actions that don't fit into any other category.
 */
@IgorCategory("Util")
public interface UtilAction extends Action {

    /**
     * The (default) time format to use for parsing data timestamps.
     */
    String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
}
