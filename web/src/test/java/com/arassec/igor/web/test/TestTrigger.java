package com.arassec.igor.web.test;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.trigger.BaseTrigger;
import com.arassec.igor.core.model.trigger.ScheduledTrigger;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * A trigger to test object mapping. This trigger has no parameters, which must be handled safely during object mapping.
 */
@IgorComponent(categoryId = "trigger-category-id", typeId = "trigger-type-id")
public class TestTrigger extends BaseTrigger implements ScheduledTrigger {

    /**
     * Test parameter for input validation of the trigger.
     */
    @Getter
    @Setter
    @NotNull
    @IgorParam
    private Integer testParam = 666;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCronExpression() {
        return "* 1 * * * *";
    }

}
