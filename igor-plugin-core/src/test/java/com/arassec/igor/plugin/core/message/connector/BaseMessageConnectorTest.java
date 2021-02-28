package com.arassec.igor.plugin.core.message.connector;

import com.arassec.igor.plugin.core.CorePluginCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link BaseMessageConnector}.
 */
@DisplayName("Base-Message-Connector Tests.")
class BaseMessageConnectorTest {

    /**
     * The base class under test.
     */
    private final BaseMessageConnector baseMessageConnector = mock(BaseMessageConnector.class,
            withSettings().useConstructor("type-id").defaultAnswer(CALLS_REAL_METHODS));

    /**
     * Tests initialization.
     */
    @Test
    @DisplayName("Tests initialization.")
    void testInitialization() {
        assertEquals(CorePluginCategory.MESSAGE.getId(), baseMessageConnector.getCategoryId());
        assertEquals("type-id", baseMessageConnector.getTypeId());
    }

}
