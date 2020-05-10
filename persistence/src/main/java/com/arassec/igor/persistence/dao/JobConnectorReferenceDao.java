package com.arassec.igor.persistence.dao;

import com.arassec.igor.persistence.entity.JobConnectorReferenceEntity;
import com.arassec.igor.persistence.entity.JobConnectorReferenceIdentity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Defines access to job-connector-references in the database.
 */
@Repository
public interface JobConnectorReferenceDao extends PagingAndSortingRepository<JobConnectorReferenceEntity,
        JobConnectorReferenceIdentity> {

    /**
     * Finds all job-connector-references for the given job ID.
     *
     * @param jobId The job's ID.
     *
     * @return List of job-connector-references.
     */
    @Query(value = "SELECT * FROM job_connector_reference WHERE job_id = :jobId", nativeQuery = true)
    List<JobConnectorReferenceEntity> findByJobId(@Param("jobId") String jobId);

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
     *
     * @return List of job-connector-references.
     */
    @Query(value = "SELECT * FROM job_connector_reference WHERE connector_id = :connectorId", nativeQuery = true)
    Page<JobConnectorReferenceEntity> findByConnectorId(@Param("connectorId") String connectorId, Pageable pageable);

    /**
     * Deletes all entities for the given connector ID.
     *
     * @param connectorId The connector's ID.
     */
    @Modifying
    @Query(value = "DELETE FROM job_connector_reference WHERE connector_id = :connectorId", nativeQuery = true)
    void deleteByConnectorId(@Param("connectorId") String connectorId);

}
