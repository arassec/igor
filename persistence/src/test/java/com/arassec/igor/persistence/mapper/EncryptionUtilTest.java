package com.arassec.igor.persistence.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link EncryptionUtil}.
 */
@DisplayName("Tests for encrypting and decrypting secured parameters.")
class EncryptionUtilTest {

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
        EncryptionUtil encryptionUtil = new EncryptionUtil();
        ReflectionTestUtils.setField(encryptionUtil, "password", "password");
        encryptionUtil.afterPropertiesSet();
        String encryptedTestString = encryptionUtil.encrypt(CLEARTEXT_TEST_STRING);
        assertEquals(CLEARTEXT_TEST_STRING, encryptionUtil.decrypt(encryptedTestString));
    }

}
