package com.arassec.igor.persistence.dao;

import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.persistence.entity.ConnectorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Defines access to {@link Connector}s in the database.
 */
@Repository
public interface ConnectorDao extends PagingAndSortingRepository<ConnectorEntity, String> {

    /**
     * Finds a connector entity by its name.
     *
     * @param name The connector's name.
     *
     * @return The connector.
     */
    ConnectorEntity findByName(String name);

    /**
     * Finds a page of connectors that match the supplied name.
     *
     * @param name     The name part to search for.
     * @param pageable The page parameters to use.
     *
     * @return The page of entities.
     */
    Page<ConnectorEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Returns a connector's name by its ID.
     *
     * @param id The connector's ID.
     *
     * @return The connector's name.
     */
    @Query(value = "SELECT name FROM connector WHERE id = :id", nativeQuery = true)
    String findNameById(@Param("id") String id);

}
