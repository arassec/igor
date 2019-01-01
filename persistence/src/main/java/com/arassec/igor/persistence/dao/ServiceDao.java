package com.arassec.igor.persistence.dao;

import com.arassec.igor.persistence.entity.ServiceEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Defines access to {@link com.arassec.igor.core.model.service.Service}s in the database.
 */
@Repository
public interface ServiceDao extends CrudRepository<ServiceEntity, Long> {
}
