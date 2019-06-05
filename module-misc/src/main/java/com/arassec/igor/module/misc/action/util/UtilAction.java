package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.IgorActionCategory;

/**
 * Defines methods for actions that don't fit into any other category.
 */
@IgorActionCategory(label = "Util")
public interface UtilAction {

    /**
     * The (default) time format to use for parsing data timestamps.
     */
    String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
}
