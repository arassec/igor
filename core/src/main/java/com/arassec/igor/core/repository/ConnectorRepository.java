package com.arassec.igor.core.repository;

import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;

import java.util.List;

/**
 * Repository for {@link Connector}s.
 */
public interface ConnectorRepository {

    /**
     * Saves a new connector or updates an existing one.
     *
     * @param connector The connector to save.
     *
     * @return The saved connector.
     */
    Connector upsert(Connector connector);

    /**
     * Returns all connectors.
     *
     * @return The list of all available connectors.
     */
    List<Connector> findAll();

    /**
     * Returns a subset of all connectors matching the supplied criteria.
     *
     * @param pageNumber The page number to load.
     * @param pageSize   The page size.
     * @param nameFilter An optional filter for the connector's name.
     *
     * @return The page with connectors.
     */
    ModelPage<Connector> findPage(int pageNumber, int pageSize, String nameFilter);

    /**
     * Finds a connector by its ID.
     *
     * @param id The connector's ID.
     *
     * @return The {@link Connector}.
     */
    Connector findById(String id);

    /**
     * Finds a connector by its name.
     *
     * @param name The connector's name.
     *
     * @return The {@link Connector} with this name.
     */
    Connector findByName(String name);

    /**
     * Deletes a connector by its ID.
     *
     * @param id The ID of the connector to delete.
     */
    void deleteById(String id);

    /**
     * Returns all jobs that use the given connector.
     *
     * @param id         The connector's ID.
     * @param pageNumber The page to load.
     * @param pageSize   The size of the page.
     *
     * @return List of job IDs and names that are using this connector.
     */
    ModelPage<Pair<String, String>> findReferencingJobs(String id, int pageNumber, int pageSize);

}
