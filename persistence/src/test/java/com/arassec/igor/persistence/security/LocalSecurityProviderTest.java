package com.arassec.igor.persistence.security;

import com.arassec.igor.persistence.IgorPersistenceProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link LocalSecurityProvider}.
 */
@DisplayName("Tests the local security-provider.")
class LocalSecurityProviderTest {

    /**
     * Cleartext string.
     */
    private static final String CLEARTEXT_TEST_STRING = "igor-encryption-test";

    /**
     * Tests String encryption.
     */
    @Test
    @DisplayName("Tests encrypting a string and decrypting it afterwards.")
    void testEncryptionAndDecryption() {
        IgorPersistenceProperties igorPersistenceProperties = new IgorPersistenceProperties();
        igorPersistenceProperties.setLocalSecurityToken("local-security-test-token");

        LocalSecurityProvider localSecurityProvider = new LocalSecurityProvider(igorPersistenceProperties);

        String encryptedTestString = localSecurityProvider.encrypt("id", "paramName", CLEARTEXT_TEST_STRING);

        assertEquals(CLEARTEXT_TEST_STRING, localSecurityProvider.decrypt("id", "paramName", encryptedTestString));
    }

}
