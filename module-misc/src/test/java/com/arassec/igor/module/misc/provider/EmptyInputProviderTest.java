package com.arassec.igor.module.misc.provider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link EmptyInputProvider}.
 */
@DisplayName("'Empty input provider' tests.")
class EmptyInputProviderTest {

    /**
     * Tests the provider.
     */
    @Test
    @DisplayName("Tests the retrieval of empty data.")
    void testProvide() {
        EmptyInputProvider provider = new EmptyInputProvider();
        provider.setAmount(3);
        assertEquals(3, provider.getAmount());
        for (int i = 0; i < 3; i++) {
            assertTrue(provider.hasNext());
            assertNotNull(provider.next());
        }
        assertFalse(provider.hasNext());
    }

}