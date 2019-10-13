package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.action.BaseAction;

import java.util.Map;

/**
 * Base class for file based actions.
 */
public abstract class BaseFileAction extends BaseAction {

    /**
     * Resolves a directory from the supplied data.
     *
     * @param data  The data.
     * @param query The query to get the directory with.
     *
     * @return A directory (or path) as String.
     */
    protected String resolveDirectory(Map<String, Object> data, String query) {
        String resolvedDirectory = getString(data, query);

        if (resolvedDirectory == null) {
            resolvedDirectory = "/";
        }

        if (!resolvedDirectory.endsWith("/")) {
            resolvedDirectory += "/";
        }

        return resolvedDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCategoryId() {
        return "28ac6145-b4d8-4d09-8e99-b35ac24aae22";
    }

}
