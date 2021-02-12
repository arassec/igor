package com.arassec.igor.persistence.mapper;

import com.arassec.igor.application.registry.IgorComponentRegistry;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.repository.ConnectorRepository;
import com.arassec.igor.persistence.PersistenceConfiguration;
import com.arassec.igor.persistence.test.TestAction;
import com.arassec.igor.persistence.test.TestConnector;
import com.arassec.igor.persistence.test.TestSecurityProvider;
import com.arassec.igor.persistence.test.TestTrigger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Base class for mapping tests.
 */
public abstract class MapperBaseTest {

    /**
     * {@link ObjectMapper} for {@link Connector}s.
     */
    protected ObjectMapper connectorObjectMapper;

    /**
     * {@link ObjectMapper} for {@link com.arassec.igor.core.model.job.Job}s.
     */
    protected ObjectMapper jobObjectMapper;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        ApplicationContext applicationContextMock = mock(ApplicationContext.class);
        when(applicationContextMock.getBean(TestConnector.class)).thenReturn(new TestConnector());
        when(applicationContextMock.getBean(TestTrigger.class)).thenReturn(new TestTrigger());
        when(applicationContextMock.getBean(TestAction.class)).thenReturn(new TestAction());

        IgorComponentRegistry igorComponentRegistry = new IgorComponentRegistry(List.of(new TestAction()),
                List.of(new TestTrigger()), List.of(new TestConnector()), null);
        igorComponentRegistry.setApplicationContext(applicationContextMock);
        igorComponentRegistry.afterPropertiesSet();

        PersistenceConfiguration persistenceConfiguration = new PersistenceConfiguration();

        connectorObjectMapper = persistenceConfiguration.persistenceConnectorMapper(igorComponentRegistry, new TestSecurityProvider());

        ConnectorRepository connectorRepositoryMock = mock(ConnectorRepository.class);
        when(connectorRepositoryMock.findById(TestConnector.CONNECTOR_ID)).thenReturn(new TestConnector());

        jobObjectMapper = persistenceConfiguration.persistenceJobMapper(igorComponentRegistry, connectorRepositoryMock, new TestSecurityProvider());
    }

    /**
     * Simple equality check for two JSON Strings.
     *
     * @param firstJson  The first json to test.
     * @param secondJson The second json to test.
     *
     * @return {@code true} if the corresponding JSON objects are equal, {@code false} otherwise.
     */
    protected boolean isJsonEqual(String firstJson, String secondJson) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode firstJsonNode = objectMapper.readTree(firstJson);
        JsonNode secondJsonNode = objectMapper.readTree(secondJson);
        return firstJsonNode.equals(secondJsonNode);
    }

}
