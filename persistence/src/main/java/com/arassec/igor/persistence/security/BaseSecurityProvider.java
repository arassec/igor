package com.arassec.igor.persistence.security;

import com.arassec.igor.core.model.IgorComponent;

/**
 * Base for {@link SecurityProvider} implementations.
 */
public abstract class BaseSecurityProvider implements SecurityProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup(IgorComponent igorComponent) {
        // nothing to do here...
    }

}
