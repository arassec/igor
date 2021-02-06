package com.arassec.igor.plugin.core.message.connector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the {@link FallbackMessageConnector}.
 */
@DisplayName("Fallback-Message-Connector Tests.")
class FallbackMessageConnectorTest {

    /**
     * Tests method invocation.
     */
    @Test
    @DisplayName("Tests that every method invocation throws an exception.")
    void testMethodInvocations() {
        FallbackMessageConnector connector = new FallbackMessageConnector();

        Message message = new Message();

        assertThrows(IllegalStateException.class, () -> connector.sendMessage(null));
        assertThrows(IllegalStateException.class, () -> connector.sendMessage(message));
        assertThrows(IllegalStateException.class, connector::testConfiguration);
    }

}