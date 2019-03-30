package com.arassec.igor.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Models the identity of the job-service-reference model.
 */
@Embeddable
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Data
public class JobServiceReferenceIdentity implements Serializable {

    /**
     * The job's ID.
     */
    @Column(name = "job_id")
    private Long jobId;

    /**
     * The services's ID.
     */
    @Column(name = "service_id")
    private Long serviceId;

}
