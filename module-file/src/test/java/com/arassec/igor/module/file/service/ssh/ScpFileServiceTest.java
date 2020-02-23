package com.arassec.igor.module.file.service.ssh;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;

/**
 * Tests the {@link ScpFileService}.
 */
@DisplayName("SCP file-service Tests")
public class ScpFileServiceTest extends SshFileServiceBaseTest {

    /**
     * Initializes the test environment by starting an SSHD server.
     *
     * @throws IOException in case of filesystem errors.
     */
    @BeforeAll
    public static void initialize() throws IOException {
        initializeTestEnvironment(new ScpFileService());
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
