package com.arassec.igor.persistence.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link VoidSecurityProvider}.
 */
@DisplayName("Tests the void security-provider.")
class VoidSecurityProviderTest {

    /**
     * Tests that encrypting with the void security-provider doesn't modify the input value.
     */
    @Test
    @DisplayName("Tests that 'encrypting' returns the cleartext value.")
    void testEncrypt() {
        VoidSecurityProvider voidSecurityProvider = new VoidSecurityProvider();
        assertEquals("paramValue", voidSecurityProvider.encrypt("id", "paramName", "paramValue"));
    }

    /**
     * Tests that decrypting with the void security-provider returns the input value unmodified.
     */
    @Test
    @DisplayName("Tests that 'decrypting' returns the cleartext value.")
    void testDecrypt() {
        VoidSecurityProvider voidSecurityProvider = new VoidSecurityProvider();
        assertEquals("paramValue", voidSecurityProvider.decrypt("id", "paramName", "paramValue"));
    }

}
