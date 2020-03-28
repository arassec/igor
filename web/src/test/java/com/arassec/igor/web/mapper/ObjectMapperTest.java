package com.arassec.igor.web.mapper;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.Task;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.web.test.TestAction;
import com.arassec.igor.web.test.TestProvider;
import com.arassec.igor.web.test.TestService;
import com.arassec.igor.web.test.TestTrigger;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the Object mapping in the web layer.
 */
@DisplayName("Web-Layer ObjectMapper Tests")
public class ObjectMapperTest extends MapperBaseTest {

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
        job.setExecutionHistoryLimit(666);

        TestTrigger testTrigger = new TestTrigger();
        testTrigger.setId("trigger-id");
        job.setTrigger(testTrigger);

        Task task = new Task();
        task.setId("task-id");
        task.setName("task-name");
        task.setActive(true);
        task.setDescription("task-description");

        TestProvider testProvider = new TestProvider();
        testProvider.setId("provider-id");
        testProvider.setSimulationLimit(456);
        testProvider.setTestProviderParam("test-provider-param");
        task.setProvider(testProvider);

        TestAction testAction = new TestAction();
        testAction.setId("action-id");
        testAction.setName("action-name");
        testAction.setActive(true);

        TestService testService = new TestService();
        testService.setId(TestService.SERVICE_ID);
        testAction.setTestService(testService);

        task.getActions().add(testAction);

        job.getTasks().add(task);

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
        assertEquals(666, testJob.getExecutionHistoryLimit());

        TestTrigger testTrigger = (TestTrigger) testJob.getTrigger();

        assertEquals("trigger-id", testTrigger.getId());
        assertEquals(TestTrigger.CATEGORY_ID, testTrigger.getCategoryId());
        assertEquals(TestTrigger.TYPE_ID, testTrigger.getTypeId());

        assertEquals(1, testJob.getTasks().size());
        Task task = testJob.getTasks().get(0);

        assertEquals("task-id", task.getId());
        assertEquals("task-name", task.getName());
        assertTrue(task.isActive());
        assertEquals("task-description", task.getDescription());

        TestProvider testProvider = (TestProvider) task.getProvider();
        assertEquals("provider-id", testProvider.getId());
        assertEquals(TestProvider.CATEGORY_ID, testProvider.getCategoryId());
        assertEquals(TestProvider.TYPE_ID, testProvider.getTypeId());
        assertEquals(456, testProvider.getSimulationLimit());
        assertEquals("test-provider-param", testProvider.getTestProviderParam());

        assertEquals(1, task.getActions().size());
        TestAction testAction = (TestAction) task.getActions().get(0);

        assertEquals("action-id", testAction.getId());
        assertEquals("action-name", testAction.getName());
        assertEquals(TestAction.CATEGORY_ID, testAction.getCategoryId());
        assertEquals(TestAction.TYPE_ID, testAction.getTypeId());
        assertTrue(testAction.getTestService() instanceof TestService);
    }

    /**
     * Tests serializing a service.
     */
    @Test
    @DisplayName("Tests serializing a service.")
    @SneakyThrows(IOException.class)
    void testServiceSerialization() {
        TestService testService = new TestService();
        testService.setId(TestService.SERVICE_ID);
        testService.setName("service-name");

        Writer serializedService = new StringWriter();

        objectMapper.writeValue(serializedService, testService);

        Files.writeString(Paths.get("target/service-reference.json"), serializedService.toString());

        String serializedJson = serializedService.toString();
        String referenceJson = Files.readString(Paths.get("src/test/resources/service-reference.json"));

        assertTrue(isJsonEqual(referenceJson, serializedJson));
    }

    /**
     * Tests deserializing a service.
     */
    @Test
    @DisplayName("Tests deserializing a service.")
    @SneakyThrows(IOException.class)
    void testServiceDeserialization() {
        String serviceJson = Files.readString(Paths.get("src/test/resources/service-reference.json"));

        Service testService = objectMapper.readValue(serviceJson, Service.class);

        assertTrue(testService instanceof TestService);

        assertEquals(TestService.SERVICE_ID, testService.getId());
        assertEquals("service-name", testService.getName());
    }

}
