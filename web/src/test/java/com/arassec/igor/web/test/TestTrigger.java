package com.arassec.igor.web.test;

import com.arassec.igor.core.model.trigger.BaseTrigger;
import com.arassec.igor.core.model.trigger.ScheduledTrigger;

/**
 * A trigger to test object mapping. This trigger has no parameters, which must be handled safely during object mapping.
 */
public class TestTrigger extends BaseTrigger implements ScheduledTrigger {

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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCronExpression() {
        return "* 1 * * * *";
    }
}
