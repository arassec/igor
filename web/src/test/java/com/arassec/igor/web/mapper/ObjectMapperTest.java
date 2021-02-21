package com.arassec.igor.web.mapper;

import com.arassec.igor.application.registry.IgorComponentRegistry;
import com.arassec.igor.application.util.IgorConfigHelper;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.repository.ConnectorRepository;
import com.arassec.igor.web.WebConfiguration;
import com.arassec.igor.web.test.TestAction;
import com.arassec.igor.web.test.TestConnector;
import com.arassec.igor.web.test.TestTrigger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the Object mapping in the web layer.
 */
@DisplayName("Web-Layer ObjectMapper Tests")
class ObjectMapperTest {

    /**
     * {@link ObjectMapper} for the web layer.
     */
    protected ObjectMapper objectMapper;

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

        ConnectorRepository connectorRepositoryMock = mock(ConnectorRepository.class);
        when(connectorRepositoryMock.findById(TestConnector.CONNECTOR_ID)).thenReturn(new TestConnector());

        WebConfiguration webConfiguration = new WebConfiguration();

        objectMapper = webConfiguration.objectMapper(igorComponentRegistry, connectorRepositoryMock,
                webConfiguration.messageSource(List.of(IgorConfigHelper.createMessageSource("i18n/mapper"))));
    }

    /**
     * Tests serializing a job.
     */
    @Test
    @DisplayName("Tests serializing a job.")
    @SneakyThrows(IOException.class)
    void testJobSerialization() {
        Job job = new Job();
        job.setId("job-id");
        job.setName("job-name");
        job.setDescription("job-description");
        job.setActive(true);
        job.setHistoryLimit(666);

        TestTrigger testTrigger = new TestTrigger();
        testTrigger.setId("trigger-id");
        job.setTrigger(testTrigger);

        TestAction testAction = new TestAction();
        testAction.setId("action-id");
        testAction.setName("action-name");
        testAction.setDescription("action-description");
        testAction.setActive(true);

        TestConnector testConnector = new TestConnector();
        testConnector.setId(TestConnector.CONNECTOR_ID);
        testAction.setTestConnector(testConnector);

        job.getActions().add(testAction);

        Writer serializedJob = new StringWriter();

        objectMapper.writeValue(serializedJob, job);

        Files.writeString(Paths.get("target/job-reference.json"), serializedJob.toString());

        String serializedJson = serializedJob.toString();
        String referenceJson = Files.readString(Paths.get("src/test/resources/job-reference.json"));

        assertTrue(isJsonEqual(referenceJson, serializedJson));
    }

    /**
     * Tests deserializing a job.
     */
    @Test
    @DisplayName("Tests deserializing a job.")
    @SneakyThrows(IOException.class)
    void testJobDeserialization() {
        String jobJson = Files.readString(Paths.get("src/test/resources/job-reference.json"));

        Job testJob = objectMapper.readValue(jobJson, Job.class);

        assertEquals("job-id", testJob.getId());
        assertEquals("job-name", testJob.getName());
        assertEquals("job-description", testJob.getDescription());
        assertTrue(testJob.isActive());
        assertEquals(666, testJob.getHistoryLimit());

        TestTrigger testTrigger = (TestTrigger) testJob.getTrigger();

        assertEquals("trigger-id", testTrigger.getId());
        assertEquals(TestTrigger.CATEGORY_ID, testTrigger.getCategoryId());
        assertEquals(TestTrigger.TYPE_ID, testTrigger.getTypeId());

        assertEquals(1, testJob.getActions().size());
        TestAction testAction = (TestAction) testJob.getActions().get(0);

        assertEquals("action-id", testAction.getId());
        assertEquals("action-name", testAction.getName());
        assertEquals("action-description", testAction.getDescription());
        assertEquals(TestAction.CATEGORY_ID, testAction.getCategoryId());
        assertEquals(TestAction.TYPE_ID, testAction.getTypeId());
        assertTrue(testAction.getTestConnector() instanceof TestConnector);
    }

    /**
     * Tests serializing a connector.
     */
    @Test
    @DisplayName("Tests serializing a connector.")
    @SneakyThrows(IOException.class)
    void testConnectorSerialization() {
        TestConnector testConnector = new TestConnector();
        testConnector.setId(TestConnector.CONNECTOR_ID);
        testConnector.setName("connector-name");

        Writer serializedConnector = new StringWriter();

        objectMapper.writeValue(serializedConnector, testConnector);

        Files.writeString(Paths.get("target/connector-reference.json"), serializedConnector.toString());

        String serializedJson = serializedConnector.toString();
        String referenceJson = Files.readString(Paths.get("src/test/resources/connector-reference.json"));

        assertTrue(isJsonEqual(referenceJson, serializedJson));
    }

    /**
     * Tests deserializing a connector.
     */
    @Test
    @DisplayName("Tests deserializing a connector.")
    @SneakyThrows(IOException.class)
    void testConnectorDeserialization() {
        String connectorJson = Files.readString(Paths.get("src/test/resources/connector-reference.json"));

        Connector testConnector = objectMapper.readValue(connectorJson, Connector.class);

        assertTrue(testConnector instanceof TestConnector);

        assertEquals(TestConnector.CONNECTOR_ID, testConnector.getId());
        assertEquals("connector-name", testConnector.getName());
    }

    /**
     * Simple equality check for two JSON Strings.
     *
     * @param firstJson  The first json to test.
     * @param secondJson The second json to test.
     *
     * @return {@code true} if the corresponding JSON objects are equal, {@code false} otherwise.
     */
    private boolean isJsonEqual(String firstJson, String secondJson) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode firstJsonNode = objectMapper.readTree(firstJson);
        JsonNode secondJsonNode = objectMapper.readTree(secondJson);
        return firstJsonNode.equals(secondJsonNode);
    }

}
