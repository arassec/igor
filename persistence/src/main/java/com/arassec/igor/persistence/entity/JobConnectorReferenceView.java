package com.arassec.igor.persistence.entity;

/**
 * View on the job-connector-references containing job and connector names.
 */
public interface JobConnectorReferenceView {

    /**
     * Returns the job's ID.
     *
     * @return The job's ID.
     */
    String getJobId();

    /**
     * Returns the job's name.
     *
     * @return The job's name.
     */
    String getJobName();

    /**
     * Returns the connector's ID.
     *
     * @return The connector's ID.
     */
    String getConnectorId();

    /**
     * Returns the connector's name.
     *
     * @return The connector's name.
     */
    String getConnectorName();

}
