package com.arassec.igor.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests the {@link JobConnectorReferenceEntity}.
 */
@DisplayName("'Job-Connector-Reference-Entity'-Tests")
public class JobConnectorReferenceEntityTest {

    /**
     * Tests the default constructor.
     */
    @Test
    @DisplayName("Tests the default constructor.")
    void testDefaultConstructor() {
        JobConnectorReferenceEntity jobConnectorReferenceEntity = new JobConnectorReferenceEntity();
        assertNull(jobConnectorReferenceEntity.getJobConnectorReferenceIdentity());
    }

    /**
     * Tests the parameterized constructor.
     */
    @Test
    @DisplayName("Tests the parameterized constructor.")
    void testParameterizedConstructor() {
        JobConnectorReferenceEntity jobConnectorReferenceEntity = new JobConnectorReferenceEntity("jobId", "connectorId");
        JobConnectorReferenceIdentity jobConnectorReferenceIdentity = jobConnectorReferenceEntity.getJobConnectorReferenceIdentity();
        assertEquals("jobId", jobConnectorReferenceIdentity.getJobId());
        assertEquals("connectorId", jobConnectorReferenceIdentity.getConnectorId());
    }

}
