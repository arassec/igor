package com.arassec.igor.module.file.service.ftp;

import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.module.file.service.BaseFileService;
import com.arassec.igor.module.file.service.FileInfo;
import com.arassec.igor.module.file.service.FileStreamData;
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
 * Base class for FTP based file services.
 */
@Getter
@Setter
public abstract class BaseFtpFileService extends BaseFileService {

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
    @IgorParam
    private int port = 21;

    /**
     * The username to login with.
     */
    @IgorParam(optional = true)
    private String username;

    /**
     * The password used for authentication.
     */
    @IgorParam(optional = true, secured = true)
    private String password;

    /**
     * Activates active or passive FTP mode.
     */
    @IgorParam(optional = true)
    private boolean passiveMode = true;

    /**
     * Buffer size for copying data.
     */
    @IgorParam(optional = true)
    private int bufferSize = 1024 * 1024;

    /**
     * Timeout for the data connection.
     */
    @IgorParam(optional = true)
    private int dataTimeout = 60 * 5 * 1000;

    /**
     * Keepalive timeout for the control connection.
     */
    @IgorParam(optional = true)
    private int keepAliveTimeout = 60 * 15;

    /**
     * Set to {@code true}, if it's a Windows FTP server.
     */
    @IgorParam(optional = true)
    private boolean windowsFtp = false;

    /**
     * Creates a new component instance.
     *
     * @param typeId The type ID.
     */
    public BaseFtpFileService(String typeId) {
        super(typeId);
    }

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
            throw new ServiceException("Login to FTP server " + host + ":" + port + " failed for user: " + user);
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
                    throw new ServiceException("Could not logout from FTP server " + host + ":" + port);
                }
                ftpClient.disconnect();
            } catch (IOException e) {
                throw new ServiceException("Error during logout from FTP server " + host + ":" + port, e);
            }
        }
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
                result = Stream.of(ftpFiles).filter(Objects::nonNull).map(ftpFile -> {
                    Instant mTime = Instant.ofEpochMilli(ftpFile.getTimestamp().getTime().getTime());
                    return new FileInfo(ftpFile.getName(), formatInstant(mTime));
                }).collect(Collectors.toList());
            } else {
                result = new LinkedList<>();
            }

            disconnect(ftpClient);

            return result;
        } catch (IOException e) {
            throw new ServiceException("Could not list files in directory: " + directory, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String read(String file, WorkInProgressMonitor workInProgressMonitor) {
        FTPClient ftpClient = connect();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ftpClient.retrieveFile(file, outputStream);
            return outputStream.toString();
        } catch (IOException e) {
            throw new ServiceException("Could not read FTP file!", e);
        } finally {
            disconnect(ftpClient);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileStreamData readStream(String file, WorkInProgressMonitor workInProgressMonitor) {
        try {
            FTPClient ftpClient = connect();
            FileStreamData result = new FileStreamData();
            FTPFile[] list = ftpClient.listFiles(file);
            result.setFileSize(list[0].getSize());
            result.setData(ftpClient.retrieveFileStream(file));
            result.setSourceConnectionData(ftpClient);
            return result;
        } catch (IOException e) {
            throw new ServiceException("Could not retrieve file: " + file, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeStream(String file, FileStreamData fileStreamData, WorkInProgressMonitor workInProgressMonitor) {
        FTPClient ftpClient = connect();
        try (BufferedOutputStream outputStream = new BufferedOutputStream(ftpClient.storeFileStream(file))) {
            copyStream(fileStreamData.getData(), outputStream, fileStreamData.getFileSize(), workInProgressMonitor);
        } catch (IOException e) {
            throw new ServiceException("Could not store file: " + file, e);
        } finally {
            disconnect(ftpClient);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finalizeStream(FileStreamData fileStreamData) {
        try {
            if (fileStreamData.getSourceConnectionData() instanceof FTPClient) {
                FTPClient ftpClient = (FTPClient) fileStreamData.getSourceConnectionData();
                if (!ftpClient.completePendingCommand()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                    throw new ServiceException("FTP stream handling was not finished successful!");
                } else {
                    disconnect(ftpClient);
                }
            }
        } catch (IOException e) {
            throw new ServiceException("FTP stream handling was not successful!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String file, WorkInProgressMonitor workInProgressMonitor) {
        try {
            FTPClient ftpClient = connect();
            if (!ftpClient.deleteFile(file)) {
                throw new ServiceException("Could not delete remote FTP file " + file);
            }
            disconnect(ftpClient);
        } catch (IOException e) {
            throw new ServiceException("Could not delete FTP file!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(String source, String target, WorkInProgressMonitor workInProgressMonitor) {
        try {
            FTPClient ftpClient = connect();
            ftpClient.rename(source, target);
            disconnect(ftpClient);
        } catch (IOException e) {
            throw new ServiceException("Could not move FTP file " + source + " to " + target, e);
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

    protected abstract FTPClient connect();

}