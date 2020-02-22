package com.arassec.igor.module.file.service.ssh;

import com.arassec.igor.module.file.service.FileInfo;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base class for {@link ScpFileService} and {@link SftpFileService} tests.
 */
public abstract class SshFileServiceBaseTest {

    /**
     * The service under test. Either an {@link ScpFileService} or an {@link SftpFileService}.
     */
    private static BaseSshFileService service;

    /**
     * Root directory of the FTP server.
     */
    private static final String SSHD_ROOT = "target/ssh-test/";

    /**
     * The SSHD host.
     */
    private static final String SSHD_HOST = "localhost";

    /**
     * The SSHD username.
     */
    private static final String SSHD_USER = "igor";

    /**
     * The SSHD password.
     */
    private static final String SSHD_PASS = "password";

    /**
     * The SSHD.
     */
    private static SshServer sshd;

    /**
     * Starts an SSHD and configures the supplied service.
     *
     * @param baseSshFileService The Fileservice-Instance to configure and use for testing.
     *
     * @throws IOException In case of SSHD errors.
     */
    protected static void initializeTestEnvironment(BaseSshFileService baseSshFileService) throws IOException {
        int port = SocketUtils.findAvailableTcpPort();

        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(port);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        sshd.setPasswordAuthenticator((user, pass, serverSession) -> SSHD_USER.equals(user) && SSHD_PASS.equals(pass));
        sshd.setCommandFactory(new ScpCommandFactoryMock()); // SCP support
        sshd.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory.Builder().build())); // SFTP support
        sshd.start();

        service = baseSshFileService;
        service.setHost(SSHD_HOST);
        service.setPort(port);
        service.setUsername(SSHD_USER);
        service.setPassword(SSHD_PASS);
    }


    /**
     * Stops the SSHD server and cleans the filesystem up.
     *
     * @throws IOException In case of filesystem errors.
     */
    public static void teardownTestEnvironment() throws IOException {
        sshd.stop(true);
        FileSystemUtils.deleteRecursively(Paths.get(SSHD_ROOT));
    }

    /**
     * Tests listing files.
     */
    @Test
    @DisplayName("Tests listing files.")
    void testListFiles() {
        List<FileInfo> fileInfos = service.listFiles("src/test/resources/ssh", null);

        assertEquals(2, fileInfos.size());
        assertAll("All file infos are retrieved.",
                () -> assertEquals("alpha.txt", fileInfos.get(0).getFilename()),
                () -> assertFalse(fileInfos.get(0).getLastModified().isBlank()),
                () -> assertEquals("beta.test", fileInfos.get(1).getFilename()),
                () -> assertFalse(fileInfos.get(1).getLastModified().isBlank())
        );

        List<FileInfo> filteredFileInfos = service.listFiles("src/test/resources/ssh", "test");

        assertEquals(1, filteredFileInfos.size());
        assertAll("All file infos are retrieved.",
                () -> assertEquals("beta.test", filteredFileInfos.get(0).getFilename()),
                () -> assertFalse(filteredFileInfos.get(0).getLastModified().isBlank())
        );
    }

}
