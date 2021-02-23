package com.arassec.igor.plugin.file.connector.ssh;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;

/**
 * Tests the {@link ScpFileConnector}.
 *
 * Sonar blocker S2187 can be ignored, because there are tests executed in the base class!
 */
@SuppressWarnings("java:S2187")
@DisplayName("SCP file-connector Tests")
public class ScpFileConnectorTest extends SshFileConnectorBaseTest {

    /**
     * Initializes the test environment by starting an SSHD server.
     *
     * @throws IOException in case of filesystem errors.
     */
    @BeforeAll
    public static void initialize() throws IOException {
        initializeTestEnvironment(new ScpFileConnector());
    }

    /**
     * Tears the test environment down by stopping the SSHD server.
     *
     * @throws IOException in case of filesystem errors.
     */
    @AfterAll
    public static void teardown() throws IOException {
        teardownTestEnvironment();
    }

}
