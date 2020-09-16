package com.arassec.igor.core.model.trigger;

import com.arassec.igor.core.model.BaseIgorComponent;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getMetaData() {
        return new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getData() {
        return new HashMap<>();
    }

}
