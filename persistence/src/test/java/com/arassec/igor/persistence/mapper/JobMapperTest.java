package com.arassec.igor.persistence.mapper;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.persistence.test.TestAction;
import com.arassec.igor.persistence.test.TestConnector;
import com.arassec.igor.persistence.test.TestTrigger;
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
 * Tests {@link com.arassec.igor.core.model.job.Job} mapping.
 */
@DisplayName("Tests mapping a job.")
class JobMapperTest extends MapperBaseTest {

    /**
     * Tests serializing a job.
     */
    @Test
    @DisplayName("Tests serializing a job.")
    @SneakyThrows(IOException.class)
    void testSerialization() {
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

        jobObjectMapper.writeValue(serializedJob, job);

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
    void testDeserialization() {
        String jobJson = Files.readString(Paths.get("src/test/resources/job-reference.json"));

        Job testJob = jobObjectMapper.readValue(jobJson, Job.class);

        assertEquals("job-id", testJob.getId());
        assertEquals("job-name", testJob.getName());
        assertEquals("job-description", testJob.getDescription());
        assertTrue(testJob.isActive());
        assertEquals(666, testJob.getHistoryLimit());

        TestTrigger testTrigger = (TestTrigger) testJob.getTrigger();

        assertEquals("trigger-id", testTrigger.getId());

        assertEquals(1, testJob.getActions().size());
        TestAction testAction = (TestAction) testJob.getActions().get(0);

        assertEquals("action-id", testAction.getId());
        assertEquals("action-name", testAction.getName());
        assertEquals("action-description", testAction.getDescription());
        assertTrue(testAction.getTestConnector() instanceof TestConnector);
    }

}
