package com.arassec.igor.web.mapper;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.web.simulation.ActionProxy;
import com.arassec.igor.web.simulation.ProviderProxy;
import com.arassec.igor.web.simulation.ServiceProxy;
import com.arassec.igor.web.test.TestAction;
import com.arassec.igor.web.test.TestServiceInterface;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the web layer's ObjectMapper simulation mode.
 */
@DisplayName("Simulation-ObjectMapper Tests")
public class SimulationObjectMapperTest extends BaseMapperTest {

    /**
     * Tests job deserialization in simulation mode.
     */
    @Test
    @DisplayName("Tests job deserialization in simulation mode.")
    @SneakyThrows(IOException.class)
    void testDeserializeJob() {
        String jobJson = Files.readString(Paths.get("src/test/resources/job-reference.json"));

        Job testJob = simulationObjectMapper.readValue(jobJson, Job.class);

        assertTrue(testJob.getTasks().get(0).getProvider() instanceof ProviderProxy);

        assertTrue(testJob.getTasks().get(0).getActions().get(0) instanceof ActionProxy);

        TestServiceInterface testService = ((TestAction) ((ActionProxy) testJob.getTasks().get(0).getActions().get(0)).getDelegate()).getTestService();

        assertTrue(testService instanceof Proxy);
        assertTrue(Proxy.getInvocationHandler(testService) instanceof ServiceProxy);
    }

}