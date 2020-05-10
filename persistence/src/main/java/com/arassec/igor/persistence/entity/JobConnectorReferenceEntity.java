package com.arassec.igor.persistence.entity;

import lombok.Data;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

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
