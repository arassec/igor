package com.arassec.igor.persistence.entity;

/**
 * View on the job-connector-references containing job and connector names.
 */
public interface JobConnectorReferenceView {

    /**
     * The job's ID.
     */
    String getJobId();

    /**
     * The job's name.
     */
    String getJobName();

    /**
     * The connector's ID.
     */
    String getConnectorId();

    /**
     * The connector's name.
     */
    String getConnectorName();

}
