package com.arassec.igor.maven.test.sources.component;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.trigger.BaseTrigger;
import com.arassec.igor.maven.test.sources.type.TestType;
import lombok.Getter;
import lombok.Setter;

/**
 * Component for testing.
 */
@Getter
@Setter
@IgorComponent(categoryId = "maven-plugin-test-category-id", typeId = TestType.TEST_COMPONENT_TYPE_ID)
public class TestComponent extends BaseTrigger {

    /**
     * Parameter for testing.
     */
    @IgorParam
    private String testParamOne;

    /**
     * Another parameter for testing.
     */
    @IgorParam
    private Boolean testParamTwo;

}
