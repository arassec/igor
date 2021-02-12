package com.arassec.igor.application.manager;

import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.repository.ConnectorRepository;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.ModelPageHelper;
import com.arassec.igor.core.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages {@link Connector}s. Entry point from outside the core package to create and maintain connectors.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectorManager {

    /**
     * Repository for connectors.
     */
    private final ConnectorRepository connectorRepository;

    /**
     * Saves the provided connector.
     *
     * @param connector The connector to save.
     *
     * @return The saved connector.
     */
    public Connector save(Connector connector) {
        return connectorRepository.upsert(connector);
    }

    /**
     * Loads the connector with the given ID.
     *
     * @param id The connector's ID.
     *
     * @return The {@link Connector} with the given ID or {@code null}, if none exists.
     */
    public Connector load(String id) {
        return connectorRepository.findById(id);
    }

    /**
     * Loads a page of connectors matching the supplied criteria.
     *
     * @param pageNumber The page number.
     * @param pageSize   The page size.
     * @param nameFilter An optional name filter for the connectors.
     *
     * @return The page with connectors matching the criteria.
     */
    public ModelPage<Connector> loadPage(int pageNumber, int pageSize, String nameFilter) {
        return connectorRepository.findPage(pageNumber, pageSize, nameFilter);
    }

    /**
     * Loads a connector by its name.
     *
     * @param name The connector's name.
     *
     * @return The {@link Connector} with the given name or {@code null}, if none exists.
     */
    public Connector loadByName(String name) {
        return connectorRepository.findByName(name);
    }

    /**
     * Loads all connector of given types.
     *
     * @param typeIds    The types to filter connectors with.
     * @param pageNumber The page to load.
     * @param pageSize   The size of the page.
     *
     * @return List of connectors in the category.
     */
    public ModelPage<Connector> loadAllOfType(Set<String> typeIds, int pageNumber, int pageSize) {
        if (typeIds == null || typeIds.isEmpty()) {
            return new ModelPage<>(0, 0, 0, List.of());
        }
        List<Connector> connectors = connectorRepository.findAll();
        List<Connector> allOfType = connectors.stream()
                .filter(connector -> typeIds.contains(connector.getTypeId()))
                .sorted(Comparator.comparing(Connector::getName)).collect(Collectors.toList());
        return ModelPageHelper.getModelPage(allOfType, pageNumber, pageSize);
    }

    /**
     * Deletes the connector with the given ID.
     *
     * @param id The connector's ID.
     */
    public void deleteConnector(String id) {
        connectorRepository.deleteById(id);
    }

    /**
     * Searches for jobs referencing the connector with the given ID.
     *
     * @param id         The connector's ID.
     * @param pageNumber The page to load.
     * @param pageSize   The size of the page.
     *
     * @return Set of jobs referencing this connector.
     */
    public ModelPage<Pair<String, String>> getReferencingJobs(String id, int pageNumber, int pageSize) {
        return connectorRepository.findReferencingJobs(id, pageNumber, pageSize);
    }

}
