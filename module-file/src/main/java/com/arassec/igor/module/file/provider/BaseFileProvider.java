package com.arassec.igor.module.file.provider;

import com.arassec.igor.core.model.provider.BaseProvider;

/**
 * Base class for providers working with files.
 */
public abstract class BaseFileProvider extends BaseProvider {

    /**
     * Creates a new component instance.
     *
     * @param typeId The type ID.
     */
    public BaseFileProvider(String typeId) {
        super("file-providers", typeId);
    }

}
