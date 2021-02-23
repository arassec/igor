package com.arassec.igor.plugin.file.connector.ftp;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.file.connector.FileInfo;
import com.arassec.igor.plugin.core.file.connector.FileStreamData;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.ftpserver.ftplet.FtpException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link FtpFileConnector}.
 */
class FtpFileConnectorTest extends FtpFileConnectorBaseTest {

    /**
     * Initializes the test environment by starting an FTP server.
     */
    @BeforeAll
    public static void initialize() throws IOException, FtpException {
        initializeTestEnvironment(new FtpFileConnector(), "target/ftp-tests/", false);
    }

    /**
     * Tears the test environment down by stopping the FTP server.
     */
    @AfterAll
    public static void teardown() throws IOException {
        teardownTestEnvironment();
    }

    /**
     * Tests listing files.
     */
    @Test
    @DisplayName("Tests listing files.")
    void testListFiles() {
        List<FileInfo> fileInfos = connector.listFiles(".", null);

        assertEquals(3, fileInfos.size());
        assertAll("File infos are loaded via FTP.",
                () -> assertEquals("alpha.txt", fileInfos.get(0).getFilename()),
                () -> assertNotNull(fileInfos.get(0).getLastModified()),
                () -> assertEquals("beta.save", fileInfos.get(1).getFilename()),
                () -> assertNotNull(fileInfos.get(1).getLastModified()),
                () -> assertEquals("gamma.txt", fileInfos.get(2).getFilename()),
                () -> assertNotNull(fileInfos.get(2).getLastModified())
        );

        List<FileInfo> filteredFileInfos = connector.listFiles(".", "save");

        assertEquals(1, filteredFileInfos.size());
        assertAll("Filtered file infos are loaded via FTP.",
                () -> assertEquals("beta.save", filteredFileInfos.get(0).getFilename()),
                () -> assertNotNull(filteredFileInfos.get(0).getLastModified())
        );

        // List files in directory:
        List<FileInfo> subdirFileInfos = connector.listFiles("subdir", null);

        assertEquals(1, subdirFileInfos.size());
        assertAll("File infos from sub-directory are loaded via FTP.",
                () -> assertEquals("delta.txt", subdirFileInfos.get(0).getFilename()),
                () -> assertNotNull(subdirFileInfos.get(0).getLastModified())
        );

        // List files in not existing directory must fail safe:
        List<FileInfo> emptyFiles = connector.listFiles("not/existing/subdir", null);
        assertTrue(emptyFiles.isEmpty());
    }

    /**
     * Tests reading a file.
     */
    @Test
    @DisplayName("Tests reading a file.")
    void testRead() {
        assertEquals("DELTA-igor-ftp-connector-tests", connector.read("subdir/delta.txt"));
    }

    /**
     * Tests reading a file as stream.
     */
    @Test
    @DisplayName("Tests reading a file as stream.")
    @SneakyThrows
    void testReadStream() {
        FileStreamData fileStreamData = connector.readStream("alpha.txt");

        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(fileStreamData.getData(), stringWriter);
        assertEquals("ALPHA-igor-ftp-connector-tests", stringWriter.toString());
        assertEquals(30, fileStreamData.getFileSize());
    }

    /**
     * "Tests reading a file that doesn't exist. An {@link IgorException} must be thrown to indicate the missing file.
     */
    @Test
    @DisplayName("Tests reading a file that doesn't exist.")
    void testReadStreamFailSafe() {
        IgorException igorException = assertThrows(IgorException.class, () -> connector.readStream("invalid/not-existing.file"));
        assertEquals("Could not retrieve file: invalid/not-existing.file", igorException.getMessage());
    }

    /**
     * Tests writing a file as stream.
     */
    @Test
    @DisplayName("Tests writing a file as stream.")
    void testWriteStream() throws IOException {
        String fileName = "write-stream-test.txt";
        String fileContent = "ftp-writestream-test";

        FileStreamData fileStreamData = new FileStreamData();
        fileStreamData.setData(new ByteArrayInputStream(fileContent.getBytes()));
        fileStreamData.setFileSize(fileContent.length());

        WorkInProgressMonitor wipMon = new WorkInProgressMonitor();

        connector.writeStream(fileName, fileStreamData, wipMon,
                JobExecution.builder().executionState(JobExecutionState.RUNNING).build());

        assertEquals(100, wipMon.getProgressInPercent());
        assertEquals(fileContent, Files.readString(Paths.get(ftpRoot + fileName)));

        Files.deleteIfExists(Paths.get(ftpRoot + fileName));
    }

    /**
     * Tests finalizing a stream.
     */
    @Test
    @DisplayName("Tests finalizing a stream.")
    @SneakyThrows
    void testFinalizeStream() {
        // Test FTP error during finalization:
        FTPClient ftpClientMock = mock(FTPClient.class);

        FileStreamData fileStreamData = new FileStreamData();
        fileStreamData.setSourceConnectionData(ftpClientMock);

        assertThrows(IgorException.class, () -> connector.finalizeStream(fileStreamData));
        verify(ftpClientMock, times(1)).completePendingCommand();
        verify(ftpClientMock, times(1)).logout();
        verify(ftpClientMock, times(1)).disconnect();

        // Test regular finalization:
        ftpClientMock = mock(FTPClient.class);
        when(ftpClientMock.completePendingCommand()).thenReturn(true);
        when(ftpClientMock.isConnected()).thenReturn(true);
        when(ftpClientMock.logout()).thenReturn(true);

        fileStreamData.setSourceConnectionData(ftpClientMock);

        connector.finalizeStream(fileStreamData);

        verify(ftpClientMock, times(1)).disconnect();

        // Test exception handling:
        when(ftpClientMock.completePendingCommand()).thenThrow(new IOException("ftp-finalize-test-exception"));

        assertThrows(IgorException.class, () -> connector.finalizeStream(fileStreamData));

        // Test error during logout:
        ftpClientMock = mock(FTPClient.class);
        when(ftpClientMock.completePendingCommand()).thenReturn(true);
        when(ftpClientMock.isConnected()).thenReturn(true);
        when(ftpClientMock.logout()).thenThrow(new IOException("ftp-logout-test-exception"));

        fileStreamData.setSourceConnectionData(ftpClientMock);

        assertThrows(IgorException.class, () -> connector.finalizeStream(fileStreamData));

        // Test disconnect is called only on connected clients.
        ftpClientMock = mock(FTPClient.class);
        when(ftpClientMock.completePendingCommand()).thenReturn(true);
        when(ftpClientMock.isConnected()).thenReturn(false);

        fileStreamData.setSourceConnectionData(ftpClientMock);
        connector.finalizeStream(fileStreamData);

        verify(ftpClientMock, times(0)).disconnect();
    }

    /**
     * Tests deleting a file.
     */
    @Test
    @DisplayName("Tests deleting a file.")
    @SneakyThrows
    void testDelete() {
        Files.copy(Paths.get(ftpRoot + "beta.save"), Paths.get(ftpRoot + "beta.delete"), StandardCopyOption.REPLACE_EXISTING);
        assertTrue(Files.exists(Paths.get(ftpRoot + "beta.delete")));

        connector.delete("beta.delete");

        assertFalse(Files.exists(Paths.get(ftpRoot + "beta.delete")));
    }

    /**
     * Tests moving a file.
     */
    @Test
    @DisplayName("Tests moving a file.")
    @SneakyThrows
    void testMove() {
        Files.copy(Paths.get(ftpRoot + "beta.save"), Paths.get(ftpRoot + "beta.move"), StandardCopyOption.REPLACE_EXISTING);
        assertTrue(Files.exists(Paths.get(ftpRoot + "beta.move")));

        connector.move("beta.move", "epsilon.moved");

        assertTrue(Files.exists(Paths.get(ftpRoot + "epsilon.moved")));
        assertFalse(Files.exists(Paths.get(ftpRoot + "beta.move")));

        assertEquals("BETA-igor-ftp-connector-tests", Files.readString(Paths.get(ftpRoot + "epsilon.moved")));

        Files.deleteIfExists(Paths.get(ftpRoot + "epsilon.moved"));
    }

    /**
     * Tests testing an FTP configuration.
     */
    @Test
    @DisplayName("Tests testing an FTP configuration.")
    void testTestConfiguration() {
        assertDoesNotThrow(() -> connector.testConfiguration());
    }

    /**
     * Tests connection problem handling.
     */
    @Test
    @DisplayName("Tests connection problem handling.")
    void testConnectionProblemHandling() {
        connector.setPort(0);
        assertThrows(IgorException.class, () -> connector.connect());
    }

}
