package com.arassec.igor.persistence.security;

import org.jasypt.util.text.StrongTextEncryptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * {@link SecurityProvider} that provides symmetrical encryption and decryption of secured parameters. The encrypted parameter
 * values are stored in igor's database.
 */
@Component
@ConditionalOnClass(StrongTextEncryptor.class)
@ConditionalOnProperty(value="igor.local-security-provider.token")
public class LocalSecurityProvider extends BaseSecurityProvider implements InitializingBean {

    /**
     * Password for property encryption.
     */
    @Value("${igor.local-security-provider.token}")
    private String token;

    /**
     * Provides encryption for secured properties.
     */
    private StrongTextEncryptor textEncryptor = new StrongTextEncryptor();

    /**
     * Prepares the property encryption.
     */
    @Override
    public void afterPropertiesSet() {
        textEncryptor.setPassword(token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String encrypt(String id, String paramName, String paramValue) {
        return textEncryptor.encrypt(paramValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String decrypt(String id, String paramName, String paramValue) {
        return textEncryptor.decrypt(paramValue);
    }

}
