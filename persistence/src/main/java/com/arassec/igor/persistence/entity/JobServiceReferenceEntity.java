package com.arassec.igor.persistence.entity;

import lombok.Data;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * JPA Entity for job-service-references.
 */
@Data
@Entity
@Table(name = "job_service_reference", schema = "igor")
public class JobServiceReferenceEntity {

    /**
     * The ID.
     */
    @EmbeddedId
    private JobServiceReferenceIdentity jobServiceReferenceIdentity;

    /**
     * Creates a new {@link JobServiceReferenceEntity}.
     */
    public JobServiceReferenceEntity() {
    }

    /**
     * Creates a new {@link JobServiceReferenceEntity}.
     *
     * @param jobId     The job's ID.
     * @param serviceId The service's ID.
     */
    public JobServiceReferenceEntity(Long jobId, Long serviceId) {
        this.jobServiceReferenceIdentity = new JobServiceReferenceIdentity(jobId, serviceId);
    }

}
