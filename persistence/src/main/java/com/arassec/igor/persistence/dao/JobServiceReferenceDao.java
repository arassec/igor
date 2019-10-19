package com.arassec.igor.persistence.dao;

import com.arassec.igor.persistence.entity.JobServiceReferenceEntity;
import com.arassec.igor.persistence.entity.JobServiceReferenceIdentity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Defines access to job-service-references in the database.
 */
@Repository
public interface JobServiceReferenceDao extends PagingAndSortingRepository<JobServiceReferenceEntity,
        JobServiceReferenceIdentity> {

    /**
     * Finds all job-service-references for the given job ID.
     *
     * @param jobId The job's ID.
     * @return List of job-service-references.
     */
    @Query(value = "SELECT * FROM job_service_reference WHERE job_id = :jobId", nativeQuery = true)
    List<JobServiceReferenceEntity> findByJobId(@Param("jobId") Long jobId);

    /**
     * Deletes all entries for the given job ID.
     *
     * @param jobId The job's ID.
     */
    @Modifying
    @Query(value = "DELETE FROM job_service_reference WHERE job_id = :jobId", nativeQuery = true)
    void deleteByJobId(@Param("jobId") Long jobId);

    /**
     * Finds all job-service-references for the given service ID.
     *
     * @param serviceId The service's ID.
     * @return List of job-service-references.
     */
    @Query(value = "SELECT * FROM job_service_reference WHERE service_id = :serviceId", nativeQuery = true)
    Page<JobServiceReferenceEntity> findByServiceId(@Param("serviceId") Long serviceId, Pageable pageable);

    /**
     * Deletes all entities for the given service ID.
     *
     * @param serviceId The service's ID.
     */
    @Modifying
    @Query(value = "DELETE FROM job_service_reference WHERE service_id = :serviceId", nativeQuery = true)
    void deleteByServiceId(@Param("serviceId") Long serviceId);

}
