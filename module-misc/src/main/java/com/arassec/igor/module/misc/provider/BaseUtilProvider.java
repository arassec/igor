package com.arassec.igor.module.misc.provider;

import com.arassec.igor.core.model.provider.BaseProvider;

/**
 * Base class for utility providers.
 */
abstract class BaseUtilProvider extends BaseProvider {

    /**
     * Creates a new component instance.
     *
     * @param typeId     The type ID.
     */
    public BaseUtilProvider(String typeId) {
        super("util-providers", typeId);
    }

}
