package com.arassec.igor.application;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests the {@link IgorApplication} with end-to-end tests.
 *
 * Based on the Blog posts by Wim Deblauwe at https://www.wimdeblauwe.com/
 */
@Slf4j
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:file:./target/dbdata/igor;AUTO_SERVER=true",
        "spring.resources.static-locations=file:../web/src/main/resources/static"},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IgorEndToEndTests {

    /**
     * Regular expression matching the Cypress log output of failing tests.
     */
    private static final Pattern NUMBER_OF_FAILING_REGEX = Pattern.compile("\\s+.\\s+Failing:\\s+([0-9])+\\s+.\\R");

    /**
     * Maximum number of minutes the ent-to-end tests are allowed to take.
     */
    private static final int MAX_TOTAL_TEST_TIME_IN_MINUTES = 15;

    /**
     * The port, igor is running on during the test.
     */
    @LocalServerPort
    private int port;

    /**
     * Runs cypress end-to-end-tests against the application.
     */
    @Test
    @SneakyThrows
    void runE2ETests() {
        // Ensures that the container will be able to access the Spring Boot application that
        // is started via @SpringBootTest
        Testcontainers.exposeHostPorts(port);

        GenericContainer<?> container = createCypressContainer();

        container.start();

        CountDownLatch countDownLatch = new CountDownLatch(1);

        Results results = new Results();

        container.followOutput(outputFrame -> {

            String logLine = outputFrame.getUtf8String();

            log.info(logLine.replace("\n", ""));

            if (logLine.contains("Run Finished")) {
                countDownLatch.countDown();
            } else if (logLine.contains("Failing:")) {
                Matcher matcher = NUMBER_OF_FAILING_REGEX.matcher(logLine);
                if (matcher.matches()) {
                    results.setNumFailedTests(Integer.parseInt(matcher.group(1)));
                }
            }
        });

        boolean success = countDownLatch.await(15, TimeUnit.MINUTES);
        if (success) {
            if (results.getNumFailedTests() > 0) {
                fail("E2E tests failed");
            }
        } else {
            fail("E2E tests did not finish within " + MAX_TOTAL_TEST_TIME_IN_MINUTES + " minutes");
        }
    }

    /**
     * Creates a test container containing cypress.
     *
     * @return A test container with cypress.
     */
    private GenericContainer<?> createCypressContainer() {
        GenericContainer<?> result = new GenericContainer<>("cypress/included:5.3.0");
        result.withClasspathResourceMapping("e2e", "/e2e", BindMode.READ_WRITE);
        result.setWorkingDirectory("/e2e");
        result.addEnv("CYPRESS_baseUrl", "http://host.testcontainers.internal:" + port);
        return result;
    }

    /**
     * Stores the results of the tests.
     */
    @Data
    private static class Results {

        /**
         * Contains the number of failed tests.
         */
        private int numFailedTests;

    }

}
