package com.arassec.igor.plugin.file.connector.ftp;

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
import org.springframework.util.FileSystemUtils;
import org.springframework.util.SocketUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for {@link FtpFileConnector} and {@link FtpsFileConnector} tests.
 */
public abstract class FtpFileConnectorBaseTest {

    /**
     * The connector under test. Either {@link FtpFileConnector} or {@link FtpsFileConnector}.
     */
    protected static BaseFtpFileConnector connector;

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
    protected static String ftpRoot;

    /**
     * Starts an FTP server and configures the file connector.
     *
     * @param baseFtpFileConnector The FTP(S) file-connector instance to configure and use for testing.
     * @param ftpRootDir           Root directory for files of the test.
     * @param enableFtps           Set to {@code true} to enable FTPS.
     *
     * @throws IOException  In case of filesystem problems.
     * @throws FtpException In case of Apache Mina FTP-Server problems.
     */
    public static void initializeTestEnvironment(BaseFtpFileConnector baseFtpFileConnector, String ftpRootDir,
                                                 boolean enableFtps) throws IOException, FtpException {

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
            ssl.setSslProtocol("TLSv1.3");

            listenerFactory.setSslConfiguration(ssl.createSslConfiguration());
        }

        FtpServerFactory factory = new FtpServerFactory();
        factory.setUserManager(userManager);
        factory.addListener("default", listenerFactory.createListener());

        ftpServer = factory.createServer();
        ftpServer.start();

        connector = baseFtpFileConnector;
        connector.setHost(FTP_HOST);
        connector.setPort(port);
        connector.setUsername(FTP_USER);
        connector.setPassword(FTP_PASS);
    }

    /**
     * Stops the FTP server and cleans up the filesystem.
     *
     * @throws IOException In case of filesystem problems.
     */
    public static void teardownTestEnvironment() throws IOException {
        ftpServer.stop();
        FileSystemUtils.deleteRecursively(Paths.get(ftpRoot));
    }

}
