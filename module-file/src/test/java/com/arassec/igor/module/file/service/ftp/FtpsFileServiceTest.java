package com.arassec.igor.module.file.service.ftp;

import com.arassec.igor.core.model.service.ServiceException;
import org.apache.ftpserver.ftplet.FtpException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the {@link FtpsFileService}.
 *
 * Sadly, there is currently no reliable way to test an FTPS client. The Apache Mina ftpserver supports FTPS, but produces random
 * errors during tests which are run on travis-ci.
 *
 * The MockFtpServer project doesn't support FTPS at all.
 *
 * So here only the connection to the FTPS server is tested.
 */
public class FtpsFileServiceTest extends FtpFileServiceBaseTest {

    /**
     * Initializes the test environment by starting an FTP server.
     */
    @BeforeAll
    public static void initialize() throws IOException, FtpException {
        initializeTestEnvironment(new FtpsFileService(), "target/ftps-tests/", true);
    }

    /**
     * Tears the test environment down by stopping the FTP server.
     */
    @AfterAll
    public static void teardown() throws IOException {
        teardownTestEnvironment();
    }

    /**
     * Tests connecting to an FTPS server.
     */
    @Test
    @DisplayName("Tests connecting to an FTPS server.")
    void testConnect() {
        assertDoesNotThrow(() -> service.connect());
    }

    /**
     * Tests an alternative configuration.
     */
    @Test
    @DisplayName("Tests an alternative configuration.")
    void testAlternativeConfiguration() {
        FtpsFileService ftpsFileService = new FtpsFileService();
        ftpsFileService.setPassiveMode(true);
        ftpsFileService.setWindowsFtp(true);

        assertThrows(ServiceException.class, ftpsFileService::connect);
    }

}
