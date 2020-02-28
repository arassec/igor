package com.arassec.igor.persistence;

import com.arassec.igor.core.IgorCoreProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests the {@link IgorPersistenceProperties}.
 */
public class IgorPersistencePropertiesTest {

    /**
     * Tests the core properties default values.
     */
    @Test
    @DisplayName("Tests the core properties default values.")
    void testCorePropertiesDefaults() {
        IgorPersistenceProperties igorPersistenceProperties = new IgorPersistenceProperties();
        assertNull(igorPersistenceProperties.getLocalSecurityToken());
    }

}
