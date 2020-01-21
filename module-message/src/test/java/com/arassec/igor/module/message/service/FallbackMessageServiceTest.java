package com.arassec.igor.module.message.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the {@link FallbackMessageService}.
 */
@DisplayName("Fallback-Message-Service Tests.")
class FallbackMessageServiceTest {

    /**
     * Tests method invocation.
     */
    @Test
    @DisplayName("Tests that every method invocation throws an exception.")
    void testMethodInvocations() {
        FallbackMessageService service = new FallbackMessageService();

        assertThrows(IllegalStateException.class, () -> service.sendMessage(null));
        assertThrows(IllegalStateException.class, () -> service.sendMessage(new Message()));
        assertThrows(IllegalStateException.class, service::testConfiguration);
    }

}