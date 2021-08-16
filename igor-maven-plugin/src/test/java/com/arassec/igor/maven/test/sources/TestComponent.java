package com.arassec.igor.maven.test.sources;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.trigger.BaseTrigger;
import lombok.Getter;
import lombok.Setter;

/**
 * Component for testing.
 */
@Getter
@Setter
@IgorComponent(typeId = "maven-plugin-test-type-id", categoryId = "maven-plugin-test-category-id")
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
