package com.arassec.igor.module.file.service.localfs;

import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.module.file.service.FileInfo;
import com.arassec.igor.module.file.service.FileStreamData;
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
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link LocalFilesystemFileService}.
 */
@DisplayName("'Local filesystem' file-service tests.")
class LocalFilesystemFileServiceTest {

    /**
     * The service under test.
     */
    private LocalFilesystemFileService fileService = new LocalFilesystemFileService();

    /**
     * Tests listing files.
     */
    @Test
    @DisplayName("Tests listing files.")
    void testListFiles() {
        List<FileInfo> fileInfos = fileService.listFiles("src/test/resources/localfs", null);
        assertEquals(2, fileInfos.size());
        fileInfos.forEach(fileInfo -> {
            assertTrue("test.tmp".equals(fileInfo.getFilename()) || "alpha.txt".equals(fileInfo.getFilename()));
            assertNotNull(fileInfo.getLastModified());
        });

        fileInfos = fileService.listFiles("src/test/resources/localfs", "txt");
        assertEquals(1, fileInfos.size());
        assertEquals("alpha.txt", fileInfos.get(0).getFilename());
        assertNotNull(fileInfos.get(0).getLastModified());
    }

    /**
     * Test reading a file.
     */
    @Test
    @DisplayName("Test reading a file.")
    void testRead() {
        WorkInProgressMonitor wipMon = new WorkInProgressMonitor("read-test");
        String fileContent = fileService.read("src/test/resources/localfs/alpha.txt", wipMon);
        assertEquals("Just a test", fileContent);
        assertEquals(100, wipMon.getProgressInPercent());
    }

    /**
     * Tests reading a file as stream.
     */
    @Test
    @DisplayName("Tests reading a file as stream.")
    @SneakyThrows(IOException.class)
    void testReadStream() {
        FileStreamData fileStreamData = fileService.readStream("src/test/resources/localfs/alpha.txt", new WorkInProgressMonitor("readstream-test"));
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

        WorkInProgressMonitor wipMon = new WorkInProgressMonitor("writestream-test");
        FileStreamData fileStreamData = fileService.readStream("src/test/resources/localfs/alpha.txt", new WorkInProgressMonitor("readstream-test"));
        fileService.writeStream(targetFile.toString(), fileStreamData, wipMon);

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
        Files.copy(Paths.get("src/test/resources/localfs/alpha.txt"), Paths.get("target/delete-file-alpha.txt"), StandardCopyOption.REPLACE_EXISTING);
        assertTrue(Files.exists(Paths.get("target/delete-file-alpha.txt")));

        WorkInProgressMonitor wipMon = new WorkInProgressMonitor("delete-test");
        fileService.delete("target/delete-file-alpha.txt", wipMon);

        assertFalse(Files.exists(Paths.get("target/delete-file-alpha.txt")));
        assertEquals(100, wipMon.getProgressInPercent());
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

        Files.copy(Paths.get("src/test/resources/localfs/alpha.txt"), Paths.get(source), StandardCopyOption.REPLACE_EXISTING);
        Files.deleteIfExists(Paths.get(target));
        assertTrue(Files.exists(Paths.get(source)));
        assertFalse(Files.exists(Paths.get(target)));

        WorkInProgressMonitor wipMon = new WorkInProgressMonitor("move-test");
        fileService.move(source, target, wipMon);

        assertFalse(Files.exists(Paths.get(source)));
        assertTrue(Files.exists(Paths.get(target)));
        assertEquals(100, wipMon.getProgressInPercent());
    }

    /**
     * Tests that testing the service's configuration succeeds.
     */
    @Test
    @DisplayName("Tests that testing the service's configuration succeeds.")
    void testTestConfiguration() {
        assertDoesNotThrow(() -> fileService.testConfiguration());
    }

    /**
     * Tests exception handling of the local filesystem file service.
     */
    @Test
    @DisplayName("Tests exception handling of the local filesystem file service.")
    void testExceptionHandling() {
        String file = "target/non-existing.file";
        assertFalse(Files.exists(Paths.get(file)));

        assertThrows(ServiceException.class, () -> fileService.read(file, mock(WorkInProgressMonitor.class)));
        assertThrows(ServiceException.class, () -> fileService.readStream(file, mock(WorkInProgressMonitor.class)));
        assertThrows(ServiceException.class, () -> fileService.delete(file, mock(WorkInProgressMonitor.class)));
        assertThrows(ServiceException.class, () -> fileService.move(file, file, mock(WorkInProgressMonitor.class)));
    }

}