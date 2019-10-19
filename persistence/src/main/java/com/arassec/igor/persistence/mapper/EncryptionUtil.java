package com.arassec.igor.persistence.mapper;

import org.jasypt.util.text.StrongTextEncryptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for encryption.
 */
@Component
public class EncryptionUtil implements InitializingBean {

    /**
     * Password for property encryption.
     */
    @Value("${igor.security.parameters.key}")
    private String password;

    /**
     * Provides encryption for secured properties.
     */
    private StrongTextEncryptor textEncryptor = new StrongTextEncryptor();

    /**
     * Prepares the property encryption.
     */
    @Override
    public void afterPropertiesSet() {
        textEncryptor.setPassword(password);
    }

    /**
     * Encrypts the provided value.
     *
     * @param value The value to encrypt.
     * @return The encrypted value.
     */
    String encrypt(String value) {
        return textEncryptor.encrypt(value);
    }

    /**
     * Decrypts the provided value.
     *
     * @param value The value to decrypt.
     * @return The decrypted value.
     */
    String decrypt(String value) {
        return textEncryptor.decrypt(value);
    }

}
