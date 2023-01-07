package com.arassec.igor.application.util;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.action.MissingComponentAction;
import com.arassec.igor.core.model.connector.MissingComponentConnector;
import com.arassec.igor.core.model.trigger.BaseTrigger;
import com.arassec.igor.core.model.trigger.MissingComponentTrigger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests the {@link IgorComponentUtil}.
 */
class IgorComponentUtilTest {

    /**
     * The util under test.
     */
    private final IgorComponentUtil igorComponentUtil = new IgorComponentUtil();

    /**
     * Tests getting the type ID of an igor component.
     */
    @Test
    @DisplayName("Tests getting the type ID of an igor component.")
    void testGetTypeId() {
        assertEquals("missing-component-action", igorComponentUtil.getTypeId(new MissingComponentAction("test")));
        assertEquals("missing-component-trigger", igorComponentUtil.getTypeId(new MissingComponentTrigger("test")));
        assertEquals("missing-component-connector", igorComponentUtil.getTypeId(new MissingComponentConnector("test")));
        assertEquals("test-type-id", igorComponentUtil.getTypeId(new TestComponent()));
    }

    /**
     * Tests getting the category ID of an igor component.
     */
    @Test
    @DisplayName("Tests getting the category ID of an igor component.")
    void testGetCategoryId() {
        assertEquals("core", igorComponentUtil.getCategoryId(new MissingComponentAction("test")));
        assertEquals("core", igorComponentUtil.getCategoryId(new MissingComponentTrigger("test")));
        assertEquals("core", igorComponentUtil.getCategoryId(new MissingComponentConnector("test")));
        assertEquals("test-category-id", igorComponentUtil.getCategoryId(new TestComponent()));
        assertEquals(IgorComponent.DEFAULT_CATEGORY, igorComponentUtil.getCategoryId(new TestComponentWihtoutExplicitCategoryId()));
    }

    /**
     * Tests formatting a parameter name.
     */
    @Test
    @DisplayName("Tests formatting a parameter name.")
    void testFormatIgorParamName() {
        assertNull(igorComponentUtil.formatIgorParamName(null));
        assertEquals("", igorComponentUtil.formatIgorParamName(""));
        assertEquals(" ", igorComponentUtil.formatIgorParamName(" "));
        assertEquals("Test Param", igorComponentUtil.formatIgorParamName("testParam"));
        assertEquals("Test Param Two", igorComponentUtil.formatIgorParamName("testParamTwo"));
    }

    /**
     * A component for testing.
     */
    @IgorComponent(categoryId = "test-category-id", typeId = "test-type-id")
    private static class TestComponent extends BaseTrigger {
    }

    /**
     * A component for testing without an explicit category ID.
     */
    @IgorComponent(typeId = "test-component-without-explicit-category-id")
    private static class TestComponentWihtoutExplicitCategoryId extends BaseTrigger {
    }

}
