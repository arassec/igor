package com.arassec.igor.module.file.service.ftp;

import org.apache.ftpserver.ftplet.FtpException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

/**
 * Tests the {@link FtpFileService}.
 */
public class FtpFileServiceTest extends FtpFileServiceBaseTest {

    /**
     * Initializes the test environment by starting an FTP server.
     */
    @BeforeAll
    public static void initialize() throws IOException, FtpException {
        initializeTestEnvironment(new FtpFileService(), "target/ftp-tests/");
    }

    /**
     * Tears the test environment down by stopping the FTP server.
     */
    @AfterAll
    public static void teardown() throws IOException {
        teardownTestEnvironment();
    }

}
