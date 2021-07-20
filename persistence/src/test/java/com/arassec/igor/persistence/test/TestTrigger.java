package com.arassec.igor.persistence.test;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.trigger.BaseTrigger;

/**
 * A trigger to test object mapping. This trigger has no parameters, which must be handled safely during object mapping.
 */
@IgorComponent(typeId = "trigger-type-id", categoryId = "trigger-category-id")
public class TestTrigger extends BaseTrigger {

}
