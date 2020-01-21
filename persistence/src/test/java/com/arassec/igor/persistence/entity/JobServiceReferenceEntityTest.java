package com.arassec.igor.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests the {@link JobServiceReferenceEntity}.
 */
@DisplayName("'Job-Service-Reference-Entity'-Tests")
public class JobServiceReferenceEntityTest {

    /**
     * Tests the default constructor.
     */
    @Test
    @DisplayName("Tests the default constructor.")
    void testDefaultConstructor() {
        JobServiceReferenceEntity jobServiceReferenceEntity = new JobServiceReferenceEntity();
        assertNull(jobServiceReferenceEntity.getJobServiceReferenceIdentity());
    }

    /**
     * Tests the parameterized constructor.
     */
    @Test
    @DisplayName("Tests the parameterized constructor.")
    void testParameterizedConstructor() {
        JobServiceReferenceEntity jobServiceReferenceEntity = new JobServiceReferenceEntity("jobId", "serviceId");
        JobServiceReferenceIdentity jobServiceReferenceIdentity = jobServiceReferenceEntity.getJobServiceReferenceIdentity();
        assertEquals("jobId", jobServiceReferenceIdentity.getJobId());
        assertEquals("serviceId", jobServiceReferenceIdentity.getServiceId());
    }

}
