package com.arassec.igor.persistence.security;

import com.arassec.igor.persistence.IgorPersistenceProperties;
import org.jasypt.util.text.StrongTextEncryptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * {@link SecurityProvider} that provides symmetrical encryption and decryption of secured parameters. The encrypted parameter
 * values are stored in igor's database.
 */
@Component
@ConditionalOnClass(StrongTextEncryptor.class)
@ConditionalOnProperty("igor.persistence.local-security-token")
public class LocalSecurityProvider extends BaseSecurityProvider {

    /**
     * Provides encryption for secured properties.
     */
    private StrongTextEncryptor textEncryptor = new StrongTextEncryptor();

    /**
     * Prepares the property encryption.
     */
    public LocalSecurityProvider(IgorPersistenceProperties igorPersistenceProperties) {
        textEncryptor.setPassword(igorPersistenceProperties.getLocalSecurityToken());
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
