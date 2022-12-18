package com.arassec.igor.persistence.dao;

import com.arassec.igor.persistence.entity.JobConnectorReferenceEntity;
import com.arassec.igor.persistence.entity.JobConnectorReferenceIdentity;
import com.arassec.igor.persistence.entity.JobConnectorReferenceView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Defines access to job-connector-references in the database.
 */
@Repository
public interface JobConnectorReferenceDao extends PagingAndSortingRepository<JobConnectorReferenceEntity,
    JobConnectorReferenceIdentity>, CrudRepository<JobConnectorReferenceEntity, JobConnectorReferenceIdentity> {

    /**
     * Finds all job-connector-references for the given job ID.
     *
     * @param jobId The job's ID.
     * @return List of job-connector-references.
     */
    @Query(value = "SELECT jcr.job_id AS jobId, j.name AS jobName, jcr.connector_id AS connectorId, c.name AS connectorName " +
        "FROM job_connector_reference jcr " +
        "LEFT JOIN job j ON jcr.job_id = j.id " +
        "LEFT JOIN connector c ON jcr.connector_id = c.id " +
        "WHERE job_id = :jobId",
        countQuery = "select count(*) FROM job_connector_reference WHERE job_id = :jobId",
        nativeQuery = true)
    List<JobConnectorReferenceView> findByJobId(@Param("jobId") String jobId);

    /**
     * Deletes all entries for the given job ID.
     *
     * @param jobId The job's ID.
     */
    @Modifying
    @Query(value = "DELETE FROM job_connector_reference WHERE job_id = :jobId", nativeQuery = true)
    void deleteByJobId(@Param("jobId") String jobId);

    /**
     * Finds all job-connector-references for the given connector ID.
     *
     * @param connectorId The connector's ID.
     * @param pageable    Spring's {@link Pageable} object to support paging.
     * @return List of job-connector-references.
     */
    @Query(value = "SELECT jcr.job_id AS jobId, j.name AS jobName, jcr.connector_id AS connectorId, c.name AS connectorName " +
        "FROM job_connector_reference jcr " +
        "LEFT JOIN job j ON jcr.job_id = j.id " +
        "LEFT JOIN connector c ON jcr.connector_id = c.id " +
        "WHERE connector_id = :connectorId",
        countQuery = "select count(*) FROM job_connector_reference WHERE connector_id = :connectorId",
        nativeQuery = true)
    Page<JobConnectorReferenceView> findByConnectorId(@Param("connectorId") String connectorId, Pageable pageable);

    /**
     * Deletes all entities for the given connector ID.
     *
     * @param connectorId The connector's ID.
     */
    @Modifying
    @Query(value = "DELETE FROM job_connector_reference WHERE connector_id = :connectorId", nativeQuery = true)
    void deleteByConnectorId(@Param("connectorId") String connectorId);

}
