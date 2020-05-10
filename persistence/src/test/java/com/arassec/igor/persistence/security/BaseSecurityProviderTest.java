package com.arassec.igor.persistence.security;

import com.arassec.igor.persistence.test.TestConnector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link BaseSecurityProvider}.
 */
@DisplayName("Tests the base security-provider.")
public class BaseSecurityProviderTest {

    /**
     * The class under test.
     */
    private final BaseSecurityProvider baseSecurityProvider = mock(BaseSecurityProvider.class, CALLS_REAL_METHODS);

    /**
     * Tests the fallback cleanup method.
     */
    @Test
    @DisplayName("Tests the fallback cleanup method.")
    void testCleanup() {
        assertDoesNotThrow(() -> baseSecurityProvider.cleanup(null));
        assertDoesNotThrow(() -> baseSecurityProvider.cleanup(new TestConnector()));
    }

}
