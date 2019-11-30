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
        super("7a64ccf8-9b97-47b4-914f-e8aea9cbf0a2", typeId);
    }

}
