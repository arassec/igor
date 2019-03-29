package com.arassec.igor.core.application.converter.util;

import com.arassec.igor.core.model.IgorParam;
import org.jasypt.util.text.StrongTextEncryptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.Security;

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
    public String encrypt(String value) {
        return textEncryptor.encrypt(value);
    }

    /**
     * Decrypts the provided value.
     *
     * @param value The value to decrypt.
     * @return The decrypted value.
     */
    public String decrypt(String value) {
        return textEncryptor.decrypt(value);
    }

}
