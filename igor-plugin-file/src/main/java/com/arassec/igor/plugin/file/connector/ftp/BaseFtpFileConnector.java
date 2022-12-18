package com.arassec.igor.plugin.file.connector.ftp;

import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.file.connector.BaseFileConnector;
import com.arassec.igor.plugin.core.file.connector.FileInfo;
import com.arassec.igor.plugin.core.file.connector.FileStreamData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.util.StringUtils;

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
     * The host running the server.
     */
    @NotBlank
    @IgorParam
    private String host;

    /**
     * The port, the server is listening on.
     */
    @Positive
    @IgorParam
    private int port = 21;

    /**
     * An optional username for authentication/authorization.
     */
    @IgorParam(advanced = true)
    private String username;

    /**
     * An optional password for authentication/authorization.
     */
    @IgorParam(advanced = true, secured = true)
    private String password;

    /**
     * If checked, the connector will use FTP passive mode to connect to the FTP(S) server.
     */
    @IgorParam(advanced = true)
    private boolean passiveMode = true;

    /**
     * The size of the buffer <strong>in bytes</strong> that is used by the connector to e.g. copy files.
     */
    @Positive
    @IgorParam(advanced = true)
    private int bufferSize = 1048576;

    /**
     * A timeout <strong>in milliseconds</strong> after which a data connection will be aborted if no new data is received from
     * the FTP(S) server.
     */
    @Positive
    @IgorParam(advanced = true)
    private int dataTimeout = 300000;

    /**
     * A timeout <strong>in minutes</strong> after which the connection to the FTP(S) server will be aborted if no new data is
     * received.
     */
    @Positive
    @IgorParam(advanced = true)
    private int keepAliveTimeout = 900;

    /**
     * If checked, the connector expects the FTP(S) server to be running on a Microsoft Windows operating system.
     */
    @IgorParam(advanced = true)
    private boolean windowsFtp = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FileInfo> listFiles(String directory, String fileEnding) {
        try {
            var ftpClient = connect();

            List<FileInfo> result;

            FTPFile[] ftpFiles = ftpClient.listFiles(directory,
                ftpFile -> !StringUtils.hasText(fileEnding) || ftpFile.getName().endsWith(fileEnding));
            if (ftpFiles != null && ftpFiles.length > 0) {
                result = Stream.of(ftpFiles).filter(Objects::nonNull).filter(FTPFile::isFile).map(ftpFile -> {
                    var mTime = Instant.ofEpochMilli(ftpFile.getTimestamp().getTime().getTime());
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
        var ftpClient = connect();
        try (var outputStream = new ByteArrayOutputStream()) {
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
            var ftpClient = connect();
            var result = new FileStreamData();
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
        var ftpClient = connect();
        try (var outputStream = new BufferedOutputStream(ftpClient.storeFileStream(file))) {
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
            var ftpClient = (FTPClient) fileStreamData.getSourceConnectionData();
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
            var ftpClient = connect();
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
            var ftpClient = connect();
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
        var ftpClient = connect();
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
