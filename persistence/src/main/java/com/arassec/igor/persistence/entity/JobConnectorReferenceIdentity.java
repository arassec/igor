package com.arassec.igor.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Models the identity of the job-connector-reference model.
 */
@Embeddable
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Data
public class JobConnectorReferenceIdentity implements Serializable {

    /**
     * The job's ID.
     */
    @Column(name = "job_id")
    private String jobId;

    /**
     * The connector's ID.
     */
    @Column(name = "connector_id")
    private String connectorId;

}
