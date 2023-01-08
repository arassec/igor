package com.arassec.igor.persistence.repository;

import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.repository.ConnectorRepository;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.persistence.dao.ConnectorDao;
import com.arassec.igor.persistence.dao.JobConnectorReferenceDao;
import com.arassec.igor.persistence.entity.ConnectorEntity;
import com.arassec.igor.persistence.entity.JobConnectorReferenceView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * {@link ConnectorRepository} implementation that uses JDBC to persist {@link Connector}s.
 */
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class JdbcConnectorRepository implements ConnectorRepository {

    /**
     * Error message if reading a connector fails.
     */
    private static final String READ_CONNECTOR_ERROR = "Could not read connector!";

    /**
     * The DAO for connectors.
     */
    private final ConnectorDao connectorDao;

    /**
     * DAO for job-connector-references.
     */
    private final JobConnectorReferenceDao jobConnectorReferenceDao;

    /**
     * ObjectMapper for JSON conversion.
     */
    private final ObjectMapper persistenceConnectorMapper;

    /**
     * Saves a {@link Connector} to the database. Either creates a new connector or updates an existing one.
     *
     * @param connector The connector to save.
     */
    @Override
    public Connector upsert(Connector connector) {
        ConnectorEntity connectorEntity = null;

        if (connector.getId() == null) {
            connector.setId(UUID.randomUUID().toString());
        } else {
            connectorEntity = connectorDao.findById(connector.getId()).orElse(null);
        }

        if (connectorEntity == null) {
            connectorEntity = new ConnectorEntity();
            connectorEntity.setId(connector.getId());
        }

        connectorEntity.setName(connector.getName());

        try {
            connectorEntity.setContent(persistenceConnectorMapper.writeValueAsString(connector));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not save connector!", e);
        }

        connectorDao.save(connectorEntity);

        return connector;
    }

    /**
     * Finds a {@link Connector} by its ID.
     *
     * @param id The connector's ID.
     *
     * @return The connector.
     */
    @Override
    public Connector findById(String id) {
        Optional<ConnectorEntity> connectorEntityOptional = connectorDao.findById(id);
        if (connectorEntityOptional.isPresent()) {
            try {
                var connectorEntity = connectorEntityOptional.get();
                return persistenceConnectorMapper.readValue(connectorEntity.getContent(), Connector.class);
            } catch (IOException e) {
                throw new IllegalStateException(READ_CONNECTOR_ERROR, e);
            }
        }
        return null;
    }

    /**
     * Finds a {@link Connector} by its name.
     *
     * @param name The connector's name.
     *
     * @return The connector.
     */
    @Override
    public Connector findByName(String name) {
        var connectorEntity = connectorDao.findByName(name);
        if (connectorEntity != null) {
            try {
                return persistenceConnectorMapper.readValue(connectorEntity.getContent(), Connector.class);
            } catch (IOException e) {
                throw new IllegalStateException(READ_CONNECTOR_ERROR, e);
            }
        }
        return null;
    }

    /**
     * Finds all connectors in the database.
     *
     * @return List of connectors.
     */
    @Override
    public List<Connector> findAll() {
        List<Connector> result = new LinkedList<>();
        for (var connectorEntity : connectorDao.findAll()) {
            try {
                result.add(persistenceConnectorMapper.readValue(connectorEntity.getContent(), Connector.class));
            } catch (IOException e) {
                throw new IllegalStateException(READ_CONNECTOR_ERROR, e);
            }
        }
        return result;
    }

    /**
     * Finds all connectors for the requested page that match the optional name filter.
     *
     * @param pageNumber The page number to load.
     * @param pageSize   The page size.
     * @param nameFilter An optional filter for the connector's name.
     *
     * @return The page of connectors.
     */
    @Override
    public ModelPage<Connector> findPage(int pageNumber, int pageSize, String nameFilter) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("name"));

        Page<ConnectorEntity> page;
        if (nameFilter != null && !nameFilter.isEmpty()) {
            page = connectorDao.findByNameContainingIgnoreCase(nameFilter, pageable);
        } else {
            page = connectorDao.findAll(pageable);
        }

        if (page != null && page.hasContent()) {
            ModelPage<Connector> result = new ModelPage<>(page.getNumber(), page.getSize(), page.getTotalPages(), null);
            result.setItems(page.getContent().stream().map(connectorEntity -> {
                try {
                    return persistenceConnectorMapper.readValue(connectorEntity.getContent(), Connector.class);
                } catch (IOException e) {
                    throw new IllegalStateException(READ_CONNECTOR_ERROR, e);
                }
            }).toList());
            return result;
        }

        return new ModelPage<>(pageNumber, pageSize, 0, List.of());
    }

    /**
     * Deletes a connector by its ID.
     *
     * @param id The ID of the connector to delete.
     */
    @Override
    public void deleteById(String id) {
        connectorDao.deleteById(id);
        jobConnectorReferenceDao.deleteByConnectorId(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelPage<Pair<String, String>> findReferencingJobs(String id, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("j.name"));
        Page<JobConnectorReferenceView> connectorReferences = jobConnectorReferenceDao.findByConnectorId(id, pageable);
        if (connectorReferences != null && connectorReferences.hasContent()) {
            ModelPage<Pair<String, String>> result = new ModelPage<>(pageNumber, pageSize, connectorReferences.getTotalPages(), null);
            result.setItems(connectorReferences.get()
                    .map(reference -> new Pair<>(reference.getJobId(), reference.getJobName()))
                    .toList());
            return result;
        }
        return new ModelPage<>(pageNumber, pageSize, 0, List.of());
    }

}
