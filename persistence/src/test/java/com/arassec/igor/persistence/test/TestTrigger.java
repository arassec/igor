package com.arassec.igor.persistence.test;

import com.arassec.igor.core.model.trigger.BaseTrigger;

/**
 * A trigger to test object mapping. This trigger has no parameters, which must be handled safely during object mapping.
 */
public class TestTrigger extends BaseTrigger {

    /**
     * The category ID.
     */
    public static final String CATEGORY_ID = "trigger-category-id";

    /**
     * The type ID.
     */
    public static final String TYPE_ID = "trigger-type-id";

    /**
     * Creates a new component instance.
     */
    public TestTrigger() {
        super(CATEGORY_ID, TYPE_ID);
    }

}
