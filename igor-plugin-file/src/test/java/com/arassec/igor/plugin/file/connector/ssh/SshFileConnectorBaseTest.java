package com.arassec.igor.plugin.file.connector.ssh;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.file.connector.FileInfo;
import com.arassec.igor.plugin.core.file.connector.FileStreamData;
import lombok.SneakyThrows;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base class for {@link ScpFileConnector} and {@link SftpFileConnector} tests.
 */
abstract class SshFileConnectorBaseTest {

    /**
     * The connector under test. Either an {@link ScpFileConnector} or an {@link SftpFileConnector}.
     */
    private static BaseSshFileConnector connector;

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
     * Starts an SSHD and configures the supplied connector.
     *
     * @param baseSshFileConnector The connector instance to configure and use for testing.
     *
     * @throws IOException In case of SSHD errors.
     */
    protected static void initializeTestEnvironment(BaseSshFileConnector baseSshFileConnector) throws IOException {
        sshd = SshServer.setUpDefaultServer();
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        sshd.setPasswordAuthenticator((user, pass, serverSession) -> SSHD_USER.equals(user) && SSHD_PASS.equals(pass));
        sshd.setCommandFactory(new ScpCommandFactoryMock()); // SCP support
        sshd.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory.Builder().build())); // SFTP support
        sshd.start();

        connector = baseSshFileConnector;
        connector.setHost(SSHD_HOST);
        connector.setPort(sshd.getPort());
        connector.setUsername(SSHD_USER);
        connector.setPassword(SSHD_PASS);
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
        List<FileInfo> fileInfos = connector.listFiles("src/test/resources/ssh", null);

        assertEquals(2, fileInfos.size());
        assertAll("All file infos are retrieved.",
                () -> assertTrue("alpha.txt".equals(fileInfos.getFirst().getFilename())
                        || "beta.test".equals(fileInfos.getFirst().getFilename())),
                () -> assertFalse(fileInfos.getFirst().getLastModified().isBlank()),
                () -> assertTrue("alpha.txt".equals(fileInfos.get(1).getFilename())
                        || "beta.test".equals(fileInfos.get(1).getFilename())),
                () -> assertFalse(fileInfos.get(1).getLastModified().isBlank())
        );

        List<FileInfo> filteredFileInfos = connector.listFiles("src/test/resources/ssh", "test");

        assertEquals(1, filteredFileInfos.size());
        assertAll("All file infos are retrieved.",
                () -> assertEquals("beta.test", filteredFileInfos.getFirst().getFilename()),
                () -> assertFalse(filteredFileInfos.getFirst().getLastModified().isBlank())
        );
    }

    /**
     * Tests reading a file.
     */
    @Test
    @DisplayName("Tests reading a file.")
    void testRead() {
        assertEquals("ALPHA-igor-ssh-connector-tests",
                connector.read("src/test/resources/ssh/alpha.txt"));
        assertThrows(IgorException.class, () -> connector.read("non-existing-file"));
    }

    /**
     * Tests reading a file as stream.
     */
    @Test
    @DisplayName("Tests reading a file as stream.")
    @SneakyThrows
    void testReadStream() {
        FileStreamData fileStreamData = connector.readStream("src/test/resources/ssh/alpha.txt");

        assertEquals("ALPHA-igor-ssh-connector-tests", StreamUtils.copyToString(fileStreamData.getData(),
                Charset.defaultCharset()));

        connector.finalizeStream(fileStreamData);
    }

    /**
     * Tests writing a file from a stream.
     */
    @Test
    @DisplayName("Tests writing a file from a stream.")
    @SneakyThrows
    void testWriteStream() {
        String fileName = "target/ssh-write-stream-test.txt";
        String fileContent = "igor-ssh-write-test";

        Path fileNamePath = Paths.get(fileName); assertFalse(Files.exists(fileNamePath));

        FileStreamData fileStreamData = new FileStreamData();
        fileStreamData.setData(new ByteArrayInputStream(fileContent.getBytes()));
        fileStreamData.setFileSize(fileContent.getBytes().length);

        connector.writeStream(fileName, fileStreamData,
                new WorkInProgressMonitor(), JobExecution.builder().executionState(JobExecutionState.RUNNING).build());

        assertEquals(fileContent, Files.readString(fileNamePath));

        Files.deleteIfExists(fileNamePath);
    }

    /**
     * Tests deleting a file.
     */
    @Test
    @DisplayName("Tests deleting a file.")
    @SneakyThrows
    void testDelete() {
        String fileName = "target/ssh-delete-test.txt";
        Path fileNamePath = Paths.get(fileName);

        Files.writeString(fileNamePath, "ssh-delete-test", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        connector.delete(fileName);

        assertFalse(Files.exists(fileNamePath));
    }

    /**
     * Tests moving a file.
     */
    @Test
    @DisplayName("Tests moving a file.")
    @SneakyThrows
    void testMove() {
        String fileName = "target/ssh-move-test.txt";
        String targetFilename = fileName + ".moved";

        Path fileNamePath = Paths.get(fileName);
        Path targetFilenamePath = Paths.get(targetFilename);

        Files.writeString(fileNamePath, "ssh-move-test", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        assertTrue(Files.exists(fileNamePath));
        assertFalse(Files.exists(targetFilenamePath));

        connector.move(fileName, fileName + ".moved");

        assertFalse(Files.exists(fileNamePath));
        assertTrue(Files.exists(targetFilenamePath));

        Files.deleteIfExists(targetFilenamePath);
    }

    /**
     * Tests testing the configuration.
     */
    @Test
    @DisplayName("Tests testing the configuration.")
    void testTestConfiguration() {
        assertDoesNotThrow(() -> connector.testConfiguration());
    }

}
