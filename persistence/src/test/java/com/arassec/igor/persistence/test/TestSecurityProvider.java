package com.arassec.igor.persistence.test;

import com.arassec.igor.persistence.security.BaseSecurityProvider;

/**
 * {@link com.arassec.igor.persistence.security.SecurityProvider} for testing.
 */
public class TestSecurityProvider extends BaseSecurityProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public String encrypt(String id, String paramName, String paramValue) {
        return "TestSecurityProvider.encrypt()";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String decrypt(String id, String paramName, String paramValue) {
        return "TestSecurityProvider.decrypt()";
    }

}
