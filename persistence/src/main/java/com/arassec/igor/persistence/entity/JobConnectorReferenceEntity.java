package com.arassec.igor.persistence.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * JPA Entity for job-connector-references.
 */
@Data
@Entity
@Table(name = "job_connector_reference")
public class JobConnectorReferenceEntity {

    /**
     * The ID.
     */
    @EmbeddedId
    private JobConnectorReferenceIdentity jobConnectorReferenceIdentity;

    /**
     * Creates a new {@link JobConnectorReferenceEntity}.
     */
    @SuppressWarnings("unused")
    public JobConnectorReferenceEntity() {
    }

    /**
     * Creates a new {@link JobConnectorReferenceEntity}.
     *
     * @param jobId       The job's ID.
     * @param connectorId The connector's ID.
     */
    public JobConnectorReferenceEntity(String jobId, String connectorId) {
        this.jobConnectorReferenceIdentity = new JobConnectorReferenceIdentity(jobId, connectorId);
    }

}
