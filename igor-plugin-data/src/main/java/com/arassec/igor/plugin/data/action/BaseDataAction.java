package com.arassec.igor.plugin.data.action;

import com.arassec.igor.core.model.action.BaseAction;

/**
 * Base for actions in the data-plugin.
 */
public abstract class BaseDataAction extends BaseAction {

    /**
     * Default path into data items containing the transaction ID (if any).
     */
    protected static final String DEFAULT_TRANSACTION_ID_PATH = "meta.transactionId";

    /**
     * Default path of the JSON element containing query results.
     */
    protected static final String DEFAULT_QUERY_RESULT_PATH = "data.queryResult";

}
