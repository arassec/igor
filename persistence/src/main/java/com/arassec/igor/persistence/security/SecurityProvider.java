package com.arassec.igor.persistence.security;

import com.arassec.igor.core.model.IgorComponent;

/**
 * Defines a provider to encrypt and decrypt secured parameters.
 */
public interface SecurityProvider {

    /**
     * Encrypts the provided value.
     *
     * @param id         ID of the igor component the secured parameter belongs to.
     * @param paramName  The name of the secured parameter.
     * @param paramValue The value to encrypt.
     *
     * @return The encrypted value.
     */
    String encrypt(String id, String paramName, String paramValue);

    /**
     * Decrypts the provided value.
     *
     * @param id         ID of the igor component the secured parameter belongs to.
     * @param paramName  The name of the secured parameter.
     * @param paramValue The value to decrypt.
     *
     * @return The decrypted value.
     */
    String decrypt(String id, String paramName, String paramValue);

    /**
     * Called on deletion of the supplied {@link IgorComponent}.
     *
     * @param igorComponent The component that is about to be deleted.
     */
    void cleanup(IgorComponent igorComponent);

}
