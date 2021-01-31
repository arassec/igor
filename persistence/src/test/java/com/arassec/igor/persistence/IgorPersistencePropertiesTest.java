package com.arassec.igor.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests the {@link IgorPersistenceProperties}.
 */
class IgorPersistencePropertiesTest {

    /**
     * Tests the persistence properties default values.
     */
    @Test
    @DisplayName("Tests the persistence properties default values.")
    void testPersistencePropertiesDefaults() {
        IgorPersistenceProperties igorPersistenceProperties = new IgorPersistenceProperties();
        assertNull(igorPersistenceProperties.getLocalSecurityToken());
    }

}
