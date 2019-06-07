package com.arassec.igor.persistence.dao;

import com.arassec.igor.persistence.entity.ServiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Defines access to {@link com.arassec.igor.core.model.service.Service}s in the database.
 */
@Repository
public interface ServiceDao extends PagingAndSortingRepository<ServiceEntity, Long> {

    /**
     * Finds a service entity by its name.
     *
     * @param name The service's name.
     * @return The service.
     */
    ServiceEntity findByName(String name);

    /**
     * Finds a page of services that match the supplied name.
     *
     * @param name     The name part to search for.
     * @param pageable The page parameters to use.
     * @return The page of entities.
     */
    Page<ServiceEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Returns a service's name by its ID.
     *
     * @param id The service's ID.
     * @return The service's name.
     */
    @Query(value = "SELECT name FROM igor.service WHERE id = :id", nativeQuery = true)
    String findNameById(@Param("id") Long id);

}
