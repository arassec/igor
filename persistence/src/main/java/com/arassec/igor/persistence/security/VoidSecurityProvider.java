package com.arassec.igor.persistence.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;

/**
 * {@link SecurityProvider} that doesn't provide security at all. Can be used to disable parameter encryption.
 */
@Component
@ConditionalOnSingleCandidate(SecurityProvider.class)
public class VoidSecurityProvider extends BaseSecurityProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public String encrypt(String id, String paramName, String paramValue) {
        return paramValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String decrypt(String id, String paramName, String paramValue) {
        return paramValue;
    }

}
