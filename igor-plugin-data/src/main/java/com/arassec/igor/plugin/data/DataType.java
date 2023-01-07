package com.arassec.igor.plugin.data;

/**
 * Defines data type IDs.
 */
public final class DataType {

    /**
     * The "Commit Transaction" action type.
     */
    public static final String COMMIT_TRANSACTION_ACTION = "commit-transaction-action";

    /**
     * The "Execute Statement" action type.
     */
    public static final String EXECUTE_STATEMENT_ACTION = "execute-statement-action";

    /**
     * The "Query Data" action type.
     */
    public static final String QUERY_DATA_ACTION = "query-data-action";

    /**
     * The "Start Transaction" action type.
     */
    public static final String START_TRANSACTION_ACTION = "start-transaction-action";

    /**
     * The "JDBC Datasource" connector type.
     */
    public static final String JDBC_DATASOURCE_CONNECTOR = "jdbc-datasource-connector";

    /**
     * Creates a new instance.
     */
    private DataType() {
        // prevent instantiation.
    }

}
