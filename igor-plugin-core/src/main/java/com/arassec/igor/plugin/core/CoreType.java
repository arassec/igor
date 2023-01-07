package com.arassec.igor.plugin.core;

/**
 * Defines core type IDs.
 */
public class CoreType {

    /**
     * The "Copy File" action type.
     */
    public static final String COPY_FILE_ACTION = "copy-file-action";

    /**
     * The "Delete File" action type.
     */
    public static final String DELETE_FILE_ACTION = "delete-file-action";

    /**
     * The "List Files" action type.
     */
    public static final String LIST_FILES_ACTION = "list-files-action";

    /**
     * The "Move File" action type.
     */
    public static final String MOVE_FILE_ACTION = "move-file-action";

    /**
     * The "Read File" action type.
     */
    public static final String READ_FILE_ACTION = "read-file-action";

    /**
     * The "Local Filesystem" connector type.
     */
    public static final String LOCAL_FS_CONNECTOR = "localfs-file-connector";

    /**
     * The "Filter Persisted Value" action type.
     */
    public static final String FILTER_PERSISTED_VALUE_ACTION = "filter-persisted-value-action";

    /**
     * The "Persist Value" action type.
     */
    public static final String PERSIST_VALUE_ACTION = "persist-value-action";

    /**
     * The "Add Data" action type.
     */
    public static final String ADD_DATA_ACTION = "add-data-action";

    /**
     * The "Duplicate" action type.
     */
    public static final String DUPLICATE_ACTION = "duplicate-action";

    /**
     * The "Execute Command" action type.
     */
    public static final String EXECUTE_COMMAND_ACTION = "execute-command";

    /**
     * The "Filter by Regular Expression" action type.
     */
    public static final String FILTER_BY_REGEXP_ACTION = "filter-by-regexp-action";

    /**
     * The "Filter by Timestamp" action type.
     */
    public static final String FILTER_BY_TIMESTAMP_ACTION = "filter-by-timestamp-action";

    /**
     * The "Limit" action type.
     */
    public static final String LIMIT_ACTION = "limit-action";

    /**
     * The "Log" action type.
     */
    public static final String LOG_ACTION = "log-action";

    /**
     * The "Pause" action type.
     */
    public static final String PAUSE_ACTION = "pause-action";

    /**
     * The "Skip" action type.
     */
    public static final String SKIP_ACTION = "skip-action";

    /**
     * The "Sort by Timestamp-Pattern" action type.
     */
    public static final String SORT_BY_TIMESTAMP_PATTERN_ACTION = "sort-by-timestamp-pattern-action";

    /**
     * The "Split Array" action type.
     */
    public static final String SPLIT_ARRAY_ACTION = "split-array-action";

    /**
     * The "CRON" trigger type.
     */
    public static final String CRON_TRIGGER = "cron-trigger";

    /**
     * The "Manual" trigger type.
     */
    public static final String MANUAL_TRIGGER = "manual-trigger";

    /**
     * The "HTTP File Download" action type.
     */
    public static final String HTTP_FILE_DOWNLOAD_ACTION = "http-file-download-action";

    /**
     * The "HTTP Request" action type.
     */
    public static final String HTTP_REQUEST_ACTION = "http-request-action";

    /**
     * The "HTTP" connector type.
     */
    public static final String HTTP_WEB_CONNECTOR = "http-web-connector";

    /**
     * The "Web-Hook" trigger type.
     */
    public static final String WEB_HOOK_TRIGGER = "web-hook-trigger";

    /**
     * Creates a new instance.
     */
    private CoreType() {
        // prevent instantiation.
    }

}
