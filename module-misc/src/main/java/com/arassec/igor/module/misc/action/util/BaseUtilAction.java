package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.IgorCategory;
import com.arassec.igor.core.model.action.BaseAction;

/**
 * Base class for actions that don't really fit into any other category.
 */
@IgorCategory("Util")
public abstract class BaseUtilAction extends BaseAction {

    /**
     * The (default) time format to use for parsing data timestamps.
     */
    String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

}
