package com.arassec.igor.plugin.file.connector.ftp;

import com.arassec.igor.core.util.IgorException;
import org.apache.ftpserver.ftplet.FtpException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.io.IOException;

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
class FtpsFileConnectorTest extends FtpFileConnectorBaseTest {

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
     * <p>
     * The test is flaky during CI builds and hence disabled there...
     */
    @Test
    @DisplayName("Tests connecting to an FTPS server.")
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    void testConnect() {
        Assertions.assertDoesNotThrow(() -> connector.connect());
    }

    /**
     * Tests an alternative configuration.
     */
    @Test
    @DisplayName("Tests an alternative configuration.")
    void testAlternativeConfiguration() {
        FtpsFileConnector ftpsFileConnector = new FtpsFileConnector();
        ftpsFileConnector.setHost("localhost");
        ftpsFileConnector.setPassiveMode(true);
        ftpsFileConnector.setWindowsFtp(true);

        assertThrows(IgorException.class, ftpsFileConnector::connect);
    }

}
