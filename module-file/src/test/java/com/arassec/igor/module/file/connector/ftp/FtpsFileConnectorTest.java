package com.arassec.igor.module.file.connector.ftp;

import com.arassec.igor.core.util.IgorException;
import org.apache.ftpserver.ftplet.FtpException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the {@link FtpsFileConnector}.
 * <p>
 * Sadly, there is currently no reliable way to test an FTPS client. The Apache Mina ftpserver supports FTPS, but produces random
 * errors during tests which are run on travis-ci.
 * <p>
 * The MockFtpServer project doesn't support FTPS at all.
 * <p>
 * So here only the connection to the FTPS server is tested.
 */
public class FtpsFileConnectorTest extends FtpFileConnectorBaseTest {

    /**
     * Initializes the test environment by starting an FTP server.
     */
    @BeforeAll
    public static void initialize() throws IOException, FtpException {
        initializeTestEnvironment(new FtpsFileConnector(), "target/ftps-tests/", true);
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
        assertDoesNotThrow(() -> connector.connect());
    }

    /**
     * Tests an alternative configuration.
     */
    @Test
    @DisplayName("Tests an alternative configuration.")
    void testAlternativeConfiguration() {
        FtpsFileConnector ftpsFileConnector = new FtpsFileConnector();
        ftpsFileConnector.setPassiveMode(true);
        ftpsFileConnector.setWindowsFtp(true);

        assertThrows(IgorException.class, ftpsFileConnector::connect);
    }

}
