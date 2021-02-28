package com.arassec.igor.plugin.core.message.action;

import com.arassec.igor.plugin.core.CorePluginCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link BaseMessageAction}.
 */
@DisplayName("Base-Message-Action Tests.")
class BaseMessageActionTest {

    /**
     * The base class under test.
     */
    private final BaseMessageAction baseMessageAction = mock(BaseMessageAction.class,
        withSettings().useConstructor("type-id").defaultAnswer(CALLS_REAL_METHODS));

    /**
     * Tests initialization.
     */
    @Test
    @DisplayName("Tests initialization.")
    void testInitialization() {
        assertEquals(CorePluginCategory.MESSAGE.getId(), baseMessageAction.getCategoryId());
        assertEquals("type-id", baseMessageAction.getTypeId());
    }

}
