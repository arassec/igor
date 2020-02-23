package com.arassec.igor.module.file.service.ftp;

import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.module.file.service.FileInfo;
import com.arassec.igor.module.file.service.FileStreamData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.SocketUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Base class for {@link FtpFileService} and {@link FtpsFileService} tests.
 */
@Slf4j
public abstract class FtpFileServiceBaseTest {

    /**
     * The service under test. Either FTP-Fileservice or FTPS-Fileservice.
     */
    protected static BaseFtpFileService service;

    /**
     * The FTP host.
     */
    private static final String FTP_HOST = "localhost";

    /**
     * The FTP username.
     */
    private static final String FTP_USER = "igor";

    /**
     * The FTP password.
     */
    private static final String FTP_PASS = "password";

    /**
     * The FTP server for testing.
     */
    private static FtpServer ftpServer;

    /**
     * Root directory for files of the test.
     */
    private static String ftpRoot;

    /**
     * Starts an FTP server and configures the file service.
     *
     * @param baseFtpFileService The FTP(S)-Fileservice-Instance to configure and use for testing.
     * @param ftpRootDir Root directory for files of the test.
     * @param enableFtps Set to {@code true} to enable FTPS.
     *
     * @throws IOException  In case of filesystem problems.
     * @throws FtpException In case of Apache Mina FTP-Server problems.
     */
    protected static void initializeTestEnvironment(BaseFtpFileService baseFtpFileService, String ftpRootDir,
                                                    boolean enableFtps) throws IOException,
            FtpException {

        // The FTP basedir is copied to test deletion and file upload.
        ftpRoot = ftpRootDir;
        FileSystemUtils.copyRecursively(Paths.get("src/test/resources/ftp/igor"), Paths.get(ftpRoot));

        int port = SocketUtils.findAvailableTcpPort();

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        UserManager userManager = userManagerFactory.createUserManager();
        BaseUser user = new BaseUser();
        user.setName(FTP_USER);
        user.setPassword(FTP_PASS);
        user.setHomeDirectory(ftpRoot);

        List<Authority> authorities = new ArrayList<>();
        authorities.add(new WritePermission());
        user.setAuthorities(authorities);

        userManager.save(user);

        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setServerAddress(FTP_HOST);
        listenerFactory.setPort(port);

        if (enableFtps) {
            SslConfigurationFactory ssl = new SslConfigurationFactory();
            ssl.setKeystoreFile(new File("src/test/resources/igor-tests-keystore.jks"));
            ssl.setKeystorePassword("password");

            listenerFactory.setSslConfiguration(ssl.createSslConfiguration());
        }

        FtpServerFactory factory = new FtpServerFactory();
        factory.setUserManager(userManager);
        factory.addListener("default", listenerFactory.createListener());

        ftpServer = factory.createServer();
        ftpServer.start();

        service = baseFtpFileService;
        service.setHost(FTP_HOST);
        service.setPort(port);
        service.setUsername(FTP_USER);
        service.setPassword(FTP_PASS);
    }

    /**
     * Stops the FTP server and cleans up the filesystem.
     *
     * @throws IOException  In case of filesystem problems.
     */
    public static void teardownTestEnvironment() throws IOException {
        ftpServer.stop();
        FileSystemUtils.deleteRecursively(Paths.get(ftpRoot));
    }

    /**
     * Tests listing files.
     */
    @Test
    @DisplayName("Tests listing files.")
    void testListFiles() {
        log.info("testListFiles");
        List<FileInfo> fileInfos = service.listFiles(".", null);

        assertEquals(3, fileInfos.size());
        assertAll("File infos are loaded via FTP.",
                () -> assertEquals("alpha.txt", fileInfos.get(0).getFilename()),
                () -> assertNotNull(fileInfos.get(0).getLastModified()),
                () -> assertEquals("beta.save", fileInfos.get(1).getFilename()),
                () -> assertNotNull(fileInfos.get(1).getLastModified()),
                () -> assertEquals("gamma.txt", fileInfos.get(2).getFilename()),
                () -> assertNotNull(fileInfos.get(2).getLastModified())
        );

        List<FileInfo> filteredFileInfos = service.listFiles(".", "save");

        assertEquals(1, filteredFileInfos.size());
        assertAll("Filtered file infos are loaded via FTP.",
                () -> assertEquals("beta.save", filteredFileInfos.get(0).getFilename()),
                () -> assertNotNull(filteredFileInfos.get(0).getLastModified())
        );

        // List files in directory:
        List<FileInfo> subdirFileInfos = service.listFiles("subdir", null);

        assertEquals(1, subdirFileInfos.size());
        assertAll("File infos from sub-directory are loaded via FTP.",
                () -> assertEquals("delta.txt", subdirFileInfos.get(0).getFilename()),
                () -> assertNotNull(subdirFileInfos.get(0).getLastModified())
        );

        // List files in not existing directory must fail safe:
        List<FileInfo> emptyFiles = service.listFiles("not/existing/subdir", null);
        assertTrue(emptyFiles.isEmpty());
        log.info("testListFiles");
    }

    /**
     * Tests reading a file.
     */
    @Test
    @DisplayName("Tests reading a file.")
    void testRead() {
        log.info("testRead");
        assertEquals("DELTA-igor-ftp-service-tests", service.read("subdir/delta.txt", new WorkInProgressMonitor("ftp-read-test")));
        log.info("testRead");
    }

    /**
     * Tests reading a file as stream.
     */
    @Test
    @DisplayName("Tests reading a file as stream.")
    @SneakyThrows
    void testReadStream() {
        log.info("testReadStream");

        FileStreamData fileStreamData = service.readStream("alpha.txt", new WorkInProgressMonitor("ftp-readstream-test"));

        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(fileStreamData.getData(), stringWriter);
        assertEquals("ALPHA-igor-ftp-service-tests", stringWriter.toString());
        assertEquals(28, fileStreamData.getFileSize());
        log.info("testReadStream");
    }

    /**
     * Tests writing a file as stream.
     */
    @Test
    @DisplayName("Tests writing a file as stream.")
    void testWriteStream() throws IOException {
        log.info("testWriteStream");

        String fileName = "write-stream-test.txt";
        String fileContent = "ftp-writestream-test";

        FileStreamData fileStreamData = new FileStreamData();
        fileStreamData.setData(new ByteArrayInputStream(fileContent.getBytes()));
        fileStreamData.setFileSize(fileContent.length());

        WorkInProgressMonitor wipMon = new WorkInProgressMonitor("ftp-writestream-test");

        service.writeStream(fileName, fileStreamData, wipMon);

        assertEquals(100, wipMon.getProgressInPercent());
        assertEquals(fileContent, Files.readString(Paths.get(ftpRoot + fileName)));

        Files.deleteIfExists(Paths.get(ftpRoot + fileName));
        log.info("testWriteStream");
    }

    /**
     * Tests finalizing a stream.
     */
    @Test
    @DisplayName("Tests finalizing a stream.")
    @SneakyThrows
    void testFinalizeStream() {
        log.info("testWriteStream");

        // Test FTP error during finalization:
        FTPClient ftpClientMock = mock(FTPClient.class);

        FileStreamData fileStreamData = new FileStreamData();
        fileStreamData.setSourceConnectionData(ftpClientMock);

        assertThrows(ServiceException.class, () -> service.finalizeStream(fileStreamData));
        verify(ftpClientMock, times(1)).completePendingCommand();
        verify(ftpClientMock, times(1)).logout();
        verify(ftpClientMock, times(1)).disconnect();

        // Test regular finalization:
        ftpClientMock = mock(FTPClient.class);
        when(ftpClientMock.completePendingCommand()).thenReturn(true);
        when(ftpClientMock.isConnected()).thenReturn(true);
        when(ftpClientMock.logout()).thenReturn(true);

        fileStreamData.setSourceConnectionData(ftpClientMock);

        service.finalizeStream(fileStreamData);

        verify(ftpClientMock, times(1)).disconnect();

        // Test exception handling:
        when(ftpClientMock.completePendingCommand()).thenThrow(new IOException("ftp-finalize-test-exception"));

        assertThrows(ServiceException.class, () -> service.finalizeStream(fileStreamData));

        // Test error during logout:
        ftpClientMock = mock(FTPClient.class);
        when(ftpClientMock.completePendingCommand()).thenReturn(true);
        when(ftpClientMock.isConnected()).thenReturn(true);
        when(ftpClientMock.logout()).thenThrow(new IOException("ftp-logout-test-exception"));

        fileStreamData.setSourceConnectionData(ftpClientMock);

        assertThrows(ServiceException.class, () -> service.finalizeStream(fileStreamData));

        // Test disconnect is called only on connected clients.
        ftpClientMock = mock(FTPClient.class);
        when(ftpClientMock.completePendingCommand()).thenReturn(true);
        when(ftpClientMock.isConnected()).thenReturn(false);

        fileStreamData.setSourceConnectionData(ftpClientMock);
        service.finalizeStream(fileStreamData);

        verify(ftpClientMock, times(0)).disconnect();
        log.info("testFinalizeStream");

    }

    /**
     * Tests deleting a file.
     */
    @Test
    @DisplayName("Tests deleting a file.")
    @SneakyThrows
    void testDelete() {
        log.info("testDelete");

        Files.copy(Paths.get(ftpRoot + "beta.save"), Paths.get(ftpRoot + "beta.delete"), StandardCopyOption.REPLACE_EXISTING);
        assertTrue(Files.exists(Paths.get(ftpRoot + "beta.delete")));

        service.delete("beta.delete", new WorkInProgressMonitor("ftp-delete-test"));

        assertFalse(Files.exists(Paths.get(ftpRoot + "beta.delete")));
        log.info("testDelete");
    }

    /**
     * Tests moving a file.
     */
    @Test
    @DisplayName("Tests moving a file.")
    @SneakyThrows
    void testMove() {
        log.info("testMove");

        Files.copy(Paths.get(ftpRoot + "beta.save"), Paths.get(ftpRoot + "beta.move"), StandardCopyOption.REPLACE_EXISTING);
        assertTrue(Files.exists(Paths.get(ftpRoot + "beta.move")));

        service.move("beta.move", "epsilon.moved", new WorkInProgressMonitor("ftp-move-test"));

        assertTrue(Files.exists(Paths.get(ftpRoot + "epsilon.moved")));
        assertFalse(Files.exists(Paths.get(ftpRoot + "beta.move")));

        assertEquals("BETA-igor-ftp-service-tests", Files.readString(Paths.get(ftpRoot + "epsilon.moved")));

        Files.deleteIfExists(Paths.get(ftpRoot + "epsilon.moved"));
        log.info("testMove");

    }

    /**
     * Tests testing an FTP configuration.
     */
    @Test
    @DisplayName("Tests testing an FTP configuration.")
    void testTestConfiguration() {
        log.info("testTestConfiguration");

        assertDoesNotThrow(() -> service.testConfiguration());
        log.info("testTestConfiguration");

    }

    /**
     * Tests connection problem handling.
     */
    @Test
    @DisplayName("Tests connection problem handling.")
    void testConnectionProblemHandling() {
        log.info("testConnectionProblemHandling");

        service.setPort(0);
        assertThrows(ServiceException.class, () -> service.connect());
        log.info("testTestConfiguration");

    }

    /**
     * Tests an alternative configuration.
     */
    @Test
    @DisplayName("Tests an alternative configuration.")
    void testAlternativeConfiguration() {
        log.info("testAlternativeConfiguration");

        service.setUsername(null);
        service.setPassword(null);
        service.setWindowsFtp(true);
        service.setPassiveMode(true);

        assertThrows(ServiceException.class, () -> service.connect());

        service.setUsername(FTP_USER);
        service.setPassword(FTP_PASS);
        service.setWindowsFtp(false);
        service.setPassiveMode(false);
        log.info("testAlternativeConfiguration");

    }

}
