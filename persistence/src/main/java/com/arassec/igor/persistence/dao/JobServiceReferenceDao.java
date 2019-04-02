package com.arassec.igor.persistence.dao;

import com.arassec.igor.persistence.entity.JobServiceReferenceEntity;
import com.arassec.igor.persistence.entity.JobServiceReferenceIdentity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Defines access to job-service-references in the database.
 */
@Repository
public interface JobServiceReferenceDao extends CrudRepository<JobServiceReferenceEntity, JobServiceReferenceIdentity> {

    /**
     * Finds all job-service-references for the given job ID.
     *
     * @param jobId The job's ID.
     * @return List of job-service-references.
     */
    @Query(value = "SELECT * FROM igor.job_service_reference WHERE job_id = :jobId", nativeQuery = true)
    List<JobServiceReferenceEntity> findByJobId(@Param("jobId") Long jobId);

    /**
     * Deletes all entries for the given job ID.
     *
     * @param jobId The job's ID.
     */
    @Modifying
    @Query(value = "DELETE FROM igor.job_service_reference WHERE job_id = :jobId", nativeQuery = true)
    void deleteByJobId(@Param("jobId") Long jobId);

    /**
     * Finds all job-service-references for the given service ID.
     *
     * @param serviceId The service's ID.
     * @return List of job-service-references.
     */
    @Query(value = "SELECT * FROM igor.job_service_reference WHERE service_id = :serviceId", nativeQuery = true)
    List<JobServiceReferenceEntity> findByServiceId(@Param("serviceId") Long serviceId);

    /**
     * Deletes all entities for the given service ID.
     *
     * @param serviceId The service's ID.
     */
    @Modifying
    @Query(value = "DELETE FROM igor.job_service_reference WHERE service_id = :serviceId", nativeQuery = true)
    void deleteByServiceId(@Param("serviceId") Long serviceId);

}
