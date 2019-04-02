package com.arassec.igor.persistence.dao;

import com.arassec.igor.persistence.entity.ServiceEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Defines access to {@link com.arassec.igor.core.model.service.Service}s in the database.
 */
@Repository
public interface ServiceDao extends CrudRepository<ServiceEntity, Long> {

    /**
     * Returns a service's name by its ID.
     *
     * @param id The service's ID.
     * @return The service's name.
     */
    @Query(value = "SELECT name FROM igor.service WHERE id = :id", nativeQuery = true)
    String findNameById(@Param("id") Long id);

}
