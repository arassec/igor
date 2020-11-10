package com.arassec.igor.module.file.connector.ftp;

import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.module.file.connector.BaseFileConnector;
import com.arassec.igor.module.file.connector.FileInfo;
import com.arassec.igor.module.file.connector.FileStreamData;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base class for FTP based file connectors.
 */
@Getter
@Setter
public abstract class BaseFtpFileConnector extends BaseFileConnector {

    /**
     * The host of the FTP server.
     */
    @NotBlank
    @IgorParam
    private String host;

    /**
     * The port of the FTP server.
     */
    @Positive
    @IgorParam(defaultValue = "21")
    private int port;

    /**
     * The username to login with.
     */
    @IgorParam(advanced = true)
    private String username;

    /**
     * The password used for authentication.
     */
    @IgorParam(advanced = true, secured = true)
    private String password;

    /**
     * Activates active or passive FTP mode.
     */
    @IgorParam(advanced = true, defaultValue = "true")
    private boolean passiveMode;

    /**
     * Buffer size for copying data.
     *
     * Default value: 1024 * 1024 = 1.048.576‬
     */
    @Positive
    @IgorParam(advanced = true, defaultValue = "1048576")
    private int bufferSize;

    /**
     * Timeout for the data connection.
     *
     * Default value = 60 * 5 * 1000 = 300.000‬
     */
    @Positive
    @IgorParam(advanced = true, defaultValue = "300000")
    private int dataTimeout;

    /**
     * Keepalive timeout for the control connection.
     *
     * Default value = 60 * 15 = 900
     */
    @Positive
    @IgorParam(advanced = true, defaultValue = "900")
    private int keepAliveTimeout;

    /**
     * Set to {@code true}, if it's a Windows FTP server.
     */
    @IgorParam(advanced = true, defaultValue = "false")
    private boolean windowsFtp;

    /**
     * Creates a new component instance.
     *
     * @param typeId The type ID.
     */
    protected BaseFtpFileConnector(String typeId) {
        super(typeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FileInfo> listFiles(String directory, String fileEnding) {
        try {
            FTPClient ftpClient = connect();

            List<FileInfo> result;

            FTPFile[] ftpFiles = ftpClient.listFiles(directory,
                    ftpFile -> StringUtils.isEmpty(fileEnding) || ftpFile.getName().endsWith(fileEnding));
            if (ftpFiles != null && ftpFiles.length > 0) {
                result = Stream.of(ftpFiles).filter(Objects::nonNull).filter(FTPFile::isFile).map(ftpFile -> {
                    Instant mTime = Instant.ofEpochMilli(ftpFile.getTimestamp().getTime().getTime());
                    return new FileInfo(ftpFile.getName(), formatInstant(mTime));
                }).collect(Collectors.toList());
            } else {
                result = new LinkedList<>();
            }

            disconnect(ftpClient);

            return result;
        } catch (IOException e) {
            throw new IgorException("Could not list files in directory: " + directory, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String read(String file) {
        FTPClient ftpClient = connect();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ftpClient.retrieveFile(file, outputStream);
            return outputStream.toString();
        } catch (IOException e) {
            throw new IgorException("Could not read FTP file!", e);
        } finally {
            disconnect(ftpClient);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileStreamData readStream(String file) {
        try {
            FTPClient ftpClient = connect();
            FileStreamData result = new FileStreamData();
            FTPFile[] list = ftpClient.listFiles(file);
            if (list.length == 0) {
                throw new IgorException("Could not retrieve file: " + file);
            }
            result.setFileSize(list[0].getSize());
            result.setData(ftpClient.retrieveFileStream(file));
            result.setSourceConnectionData(ftpClient);
            return result;
        } catch (IOException e) {
            throw new IgorException("Could not retrieve file: " + file, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeStream(String file, FileStreamData fileStreamData, WorkInProgressMonitor workInProgressMonitor,
                            JobExecution jobExecution) {
        FTPClient ftpClient = connect();
        try (BufferedOutputStream outputStream = new BufferedOutputStream(ftpClient.storeFileStream(file))) {
            copyStream(fileStreamData.getData(), outputStream, fileStreamData.getFileSize(), workInProgressMonitor, jobExecution);
        } catch (IOException e) {
            throw new IgorException("Could not store file: " + file, e);
        } finally {
            disconnect(ftpClient);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finalizeStream(FileStreamData fileStreamData) {
        if (fileStreamData.getSourceConnectionData() instanceof FTPClient) {
            FTPClient ftpClient = (FTPClient) fileStreamData.getSourceConnectionData();
            try {
                if (!ftpClient.completePendingCommand()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                    throw new IgorException("FTP stream handling was not finished successful!");
                } else {
                    disconnect(ftpClient);
                }
            } catch (IOException e) {
                throw new IgorException("FTP stream handling was not successful!", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String file) {
        try {
            FTPClient ftpClient = connect();
            if (!ftpClient.deleteFile(file)) {
                throw new IgorException("Could not delete remote FTP file " + file);
            }
            disconnect(ftpClient);
        } catch (IOException e) {
            throw new IgorException("Could not delete FTP file!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(String source, String target) {
        try {
            FTPClient ftpClient = connect();
            ftpClient.rename(source, target);
            disconnect(ftpClient);
        } catch (IOException e) {
            throw new IgorException("Could not move FTP file " + source + " to " + target, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration() {
        FTPClient ftpClient = connect();
        disconnect(ftpClient);
    }

    /**
     * Connects to the FTP(S)-Server. Must be implemented by subclasses.
     *
     * @return A new FTPClient instance.
     */
    protected abstract FTPClient connect();

    /**
     * Configures the FTP client and connects to the server.
     *
     * @param ftpClient The client to use.
     *
     * @throws IOException If the connection to the server fails.
     */
    void configureAndConnect(FTPClient ftpClient) throws IOException {
        ftpClient.setStrictReplyParsing(false);

        if (windowsFtp) {
            ftpClient.configure(new FTPClientConfig(FTPClientConfig.SYST_NT));
        } else {
            ftpClient.configure(new FTPClientConfig());
        }

        ftpClient.setBufferSize(bufferSize);

        ftpClient.connect(host, port);

        if (passiveMode) {
            ftpClient.enterLocalPassiveMode();
        } else {
            ftpClient.enterLocalActiveMode();
        }

        String user = username;
        String pass = password;
        if (user == null || pass == null) {
            user = "anonymous";
            pass = "igor";
        }

        if (!ftpClient.login(user, pass)) {
            throw new IgorException("Login to FTP server " + host + ":" + port + " failed for user: " + user);
        }

        ftpClient.setDataTimeout(dataTimeout);
        ftpClient.setControlKeepAliveTimeout(keepAliveTimeout);
        ftpClient.setControlKeepAliveReplyTimeout(keepAliveTimeout);
        ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
    }

    /**
     * Shuts the FTP client down.
     */
    private void disconnect(FTPClient ftpClient) {
        if (ftpClient.isConnected()) {
            try {
                if (!ftpClient.logout()) {
                    throw new IgorException("Could not logout from FTP server " + host + ":" + port);
                }
                ftpClient.disconnect();
            } catch (IOException e) {
                throw new IgorException("Error during logout from FTP server " + host + ":" + port, e);
            }
        }
    }

}
