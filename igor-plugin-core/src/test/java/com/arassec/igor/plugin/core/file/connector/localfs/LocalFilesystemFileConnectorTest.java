package com.arassec.igor.plugin.core.file.connector.localfs;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.file.connector.FileInfo;
import com.arassec.igor.plugin.core.file.connector.FileStreamData;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link LocalFilesystemFileConnector}.
 */
@DisplayName("'Local filesystem' file-connector tests.")
class LocalFilesystemFileConnectorTest {

    private static final String LOCALFS_DIR = "src/test/resources/file/localfs";

    /**
     * The connector under test.
     */
    private final LocalFilesystemFileConnector fileConnector = new LocalFilesystemFileConnector();

    /**
     * Tests listing files.
     */
    @Test
    @DisplayName("Tests listing files.")
    void testListFiles() {
        List<FileInfo> fileInfos = fileConnector.listFiles(LOCALFS_DIR, null);
        assertEquals(2, fileInfos.size());
        fileInfos.forEach(fileInfo -> {
            assertTrue("test.tmp".equals(fileInfo.getFilename()) || "alpha.txt".equals(fileInfo.getFilename()));
            assertNotNull(fileInfo.getLastModified());
        });

        fileInfos = fileConnector.listFiles(LOCALFS_DIR, "txt");
        assertEquals(1, fileInfos.size());
        assertEquals("alpha.txt", fileInfos.getFirst().getFilename());
        assertNotNull(fileInfos.getFirst().getLastModified());
    }

    /**
     * Test reading a file.
     */
    @Test
    @DisplayName("Test reading a file.")
    void testRead() {
        String fileContent = fileConnector.read(LOCALFS_DIR + "/alpha.txt");
        assertEquals("Just a test", fileContent);
    }

    /**
     * Tests reading a file as stream.
     */
    @Test
    @DisplayName("Tests reading a file as stream.")
    @SneakyThrows(IOException.class)
    void testReadStream() {
        FileStreamData fileStreamData = fileConnector.readStream(LOCALFS_DIR + "/alpha.txt");
        assertEquals(11, fileStreamData.getFileSize());
        assertEquals("Just a test", StreamUtils.copyToString(fileStreamData.getData(), StandardCharsets.UTF_8));
    }

    /**
     * Tests writing a file from stream data.
     */
    @Test
    @DisplayName("Tests writing a file from stream data.")
    @SneakyThrows(IOException.class)
    void testWriteStream() {
        Path targetFile = Paths.get("target/write-stream-alpha.txt");
        Files.deleteIfExists(targetFile);
        assertFalse(Files.exists(targetFile));

        FileStreamData fileStreamData = fileConnector.readStream(LOCALFS_DIR + "/alpha.txt");
        fileConnector.writeStream(targetFile.toString(), fileStreamData, new WorkInProgressMonitor(),
                JobExecution.builder().executionState(JobExecutionState.RUNNING).build());

        assertTrue(Files.exists(targetFile));
        assertEquals("Just a test", Files.readString(targetFile));
    }

    /**
     * Tests deleting a file.
     */
    @Test
    @DisplayName("Tests deleting a file.")
    @SneakyThrows(IOException.class)
    void testDelete() {
        Path deleteFileAlphaTxt = Paths.get("target/delete-file-alpha.txt");
        Files.copy(Paths.get(LOCALFS_DIR + "/alpha.txt"), deleteFileAlphaTxt, StandardCopyOption.REPLACE_EXISTING);
        assertTrue(Files.exists(deleteFileAlphaTxt));

        fileConnector.delete("target/delete-file-alpha.txt");

        assertFalse(Files.exists(deleteFileAlphaTxt));
    }

    /**
     * Tests moving a file.
     */
    @Test
    @DisplayName("Tests moving a file.")
    @SneakyThrows(IOException.class)
    void testMove() {
        String source = "target/move-file-alpha.txt";
        String target = "target/file-moved.txt";

        Path sourcePath = Paths.get(source);
        Path targetPath = Paths.get(target);

        Files.copy(Paths.get(LOCALFS_DIR + "/alpha.txt"), sourcePath, StandardCopyOption.REPLACE_EXISTING);
        Files.deleteIfExists(targetPath);
        assertTrue(Files.exists(sourcePath));
        assertFalse(Files.exists(targetPath));

        fileConnector.move(source, target);

        assertFalse(Files.exists(sourcePath));
        assertTrue(Files.exists(targetPath));
    }

    /**
     * Tests that testing the connector's configuration succeeds.
     */
    @Test
    @DisplayName("Tests that testing the connector's configuration succeeds.")
    void testTestConfiguration() {
        assertDoesNotThrow(fileConnector::testConfiguration);
    }

    /**
     * Tests exception handling of the local filesystem file connector.
     */
    @Test
    @DisplayName("Tests exception handling of the local filesystem file connector.")
    void testExceptionHandling() {
        String file = "target/non-existing.file";
        assertFalse(Files.exists(Paths.get(file)));
        assertThrows(IgorException.class, () -> fileConnector.read(file));
        assertThrows(IgorException.class, () -> fileConnector.readStream(file));
        assertThrows(IgorException.class, () -> fileConnector.delete(file));
        assertThrows(IgorException.class, () -> fileConnector.move(file, file));
    }

}
