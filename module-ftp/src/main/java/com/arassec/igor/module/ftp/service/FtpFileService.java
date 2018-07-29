package com.arassec.igor.module.ftp.service;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.service.file.BaseFileService;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.core.model.service.file.FileStreamData;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Fileservice that uses FTP as protocol.
 */
@IgorService(label = "FTP")
public class FtpFileService extends BaseFileService {

    /**
     * The host of the FTP server.
     */
    @IgorParam
    protected String host;

    /**
     * The port of the FTP server.
     */
    @IgorParam
    protected int port = 21;

    /**
     * The username to login with.
     */
    @IgorParam(optional = true)
    protected String username;

    /**
     * The password used for authentication.
     */
    @IgorParam(optional = true, secured = true)
    protected String password;

    /**
     * Initializes the FTP client.
     */
    protected FTPClient connect() {
        try {
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(host, port);
            String user = username;
            String pass = password;
            if (user == null || pass == null) {
                user = "anonymous";
                pass = "igor";
            }
            if (!ftpClient.login(user, pass)) {
                throw new ServiceException("Login to FTP server " + host + ":" + port + " failed for user: " + user);
            }
            return ftpClient;
        } catch (IOException e) {
            throw new ServiceException("Could not login to FTP server " + host + ":" + port, e);
        }
    }

    /**
     * Shuts the FTP client down.
     */
    private void disconnect(FTPClient ftpClient) {
        try {
            if (!ftpClient.logout()) {
                throw new ServiceException("Could not logout from FTP server " + host + ":" + port);
            }
        } catch (IOException e) {
            throw new ServiceException("Error during logout from FTP server " + host + ":" + port, e);
        } finally {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                throw new ServiceException("Could not disconnect from FTP server " + host + ":" + port, e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> listFiles(String directory) {
        try {
            FTPClient ftpClient = connect();
            List<String> result = Arrays.asList(ftpClient.listNames(directory));
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
    public String read(String file) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
            FTPClient ftpClient = connect();
            ftpClient.retrieveFile(file, outputStream);
            String result = outputStream.toString();
            disconnect(ftpClient);
            return result;
        } catch (IOException e) {
            throw new ServiceException("Could not read FTP file!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(String file, String data) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(file.getBytes())) {
            FTPClient ftpClient = connect();
            if (!ftpClient.storeFile(file, inputStream)) {
                throw new ServiceException("Could not write FTP file!");
            }
            disconnect(ftpClient);
        } catch (IOException e) {
            throw new ServiceException("Could not store file: " + file, e);
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
    public void writeStream(String file, FileStreamData fileStreamData) {
        FTPClient ftpClient = connect();
        try (BufferedOutputStream outputStream = new BufferedOutputStream(ftpClient.storeFileStream(file))) {
            copyStream(fileStreamData.getData(), outputStream, fileStreamData.getFileSize());
            disconnect(ftpClient);
        } catch (IOException e) {
            throw new ServiceException("Could not store file: " + file, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finalizeStream(FileStreamData fileStreamData) {
        try {
            if (fileStreamData.getSourceConnectionData() != null && fileStreamData.getSourceConnectionData() instanceof FTPClient) {
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
    public void delete(String file) {
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
    public void move(String source, String target) {
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
    public void testConfiguration() throws ServiceException {
        FTPClient ftpClient = connect();
        disconnect(ftpClient);
    }

}
