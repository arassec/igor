package com.arassec.igor.core.model.trigger;

import com.arassec.igor.core.model.BaseIgorComponent;

/**
 * Baseclass for Triggers.
 */
public abstract class BaseTrigger extends BaseIgorComponent implements Trigger {

    /**
     * Creates a new component instance.
     *
     * @param categoryId The category ID.
     * @param typeId     The type ID.
     */
    public BaseTrigger(String categoryId, String typeId) {
        super(categoryId, typeId);
    }

}
