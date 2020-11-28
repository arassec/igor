package com.arassec.igor.module.file.connector.ssh;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;

/**
 * Tests the {@link ScpFileConnector}.
 */
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
