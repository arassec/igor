package com.arassec.igor.plugin.core;

import lombok.Getter;

/**
 * Defines types for components of the 'core' plugin.
 */
public enum CorePluginType {

    /**
     * Type of the "Add Data" action.
     */
    ADD_DATA_ACTION("add-data-action"),

    /**
     * Type of the "Web-Hook" trigger.
     */
    WEB_HOOK_TRIGGER("web-hook-trigger"),

    /**
     * Type of the "Fallback HTTP" connector.
     */
    FALLBACK_HTTP_CONNECTOR("fallback-http-connector"),

    /**
     * Type of the "HTTP Web" connector.
     */
    HTTP_WEB_CONNECTOR("http-web-connector"),

    /**
     * Type of the "HTTP Request" action.
     */
    HTTP_REQUEST_ACTION("http-request-action"),

    /**
     * Type of the "HTTP File Download" action.
     */
    HTTP_FILE_DOWNLOAD_ACTION("http-file-download-action"),

    /**
     * Type of the "Manual" trigger.
     */
    MANUAL_TRIGGER("manual-trigger"),

    /**
     * Type of the "CRON" trigger.
     */
    CRON_TRIGGER("cron-trigger"),

    /**
     * Type of the "Split Array" action.
     */
    SPLIT_ARRAY_ACTION("split-array-action"),

    /**
     * Type of the "Sort by Timestamp-Pattern" action.
     */
    SORT_BY_TIMESTAMP_PATTERN_ACTION("sort-by-timestamp-pattern-action"),

    /**
     * Type of the "Skip" action.
     */
    SKIP_ACTION("skip-action"),

    /**
     * Type of the "Limit" action.
     */
    LIMIT_ACTION("limit-action"),

    /**
     * Type of the "Filter by Timestamp" action.
     */
    FILTER_BY_TIMESTAMP_ACTION("filter-by-timestamp-action"),

    /**
     * Type of the "Filter by Regular Expression" action.
     */
    FILTER_BY_REGEXP_ACTION("filter-by-regexp-action"),

    /**
     * Type of the "Pause" action.
     */
    PAUSE_ACTION("pause-action"),

    /**
     * Type of the "Log" action.
     */
    LOG_ACTION("log-action"),

    /**
     * Type of the "Duplicate" action.
     */
    DUPLICATE_ACTION("duplicate-action"),

    /**
     * Type of the "Persist Value" action.
     */
    PERSIST_VALUE_ACTION("persist-value-action"),

    /**
     * Type of the "Filter Persistent Value" action.
     */
    FILTER_PERSISTENT_VALUE_ACTION("filter-persisted-value-action"),

    /**
     * Type of the "Fallback File" connector.
     */
    FALLBACK_FILE_CONNECTOR("fallback-file-connector"),

    /**
     * Type of the "Local Filesystem" connector.
     */
    LOCAL_FILESYSTEM_CONNECTOR("localfs-file-connector"),

    /**
     * Type of the "Read File" action.
     */
    READ_FILE_ACTION("read-file-action"),

    /**
     * Type of the "Move File" action.
     */
    MOVE_FILE_ACTION("move-file-action"),

    /**
     * Type of the "List Files" action.
     */
    LIST_FILES_ACTION("list-files-action"),

    /**
     * Type of the "Delete File" action.
     */
    DELETE_FILE_ACTION("delete-file-action"),

    /**
     * Type of the "Copy File" action.
     */
    COPY_FILE_ACTION("copy-file-action");

    /**
     * The type's ID.
     */
    @Getter
    private final String id;

    /**
     * Creates a new type ID.
     *
     * @param id The ID to use.
     */
    CorePluginType(String id) {
        this.id = id;
    }
}
