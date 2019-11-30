package com.arassec.igor.module.file.service.ftp;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.service.ServiceException;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.io.IOException;

/**
 * File-Service that uses FTP as protocol.
 */
@ConditionalOnClass(FTPClient.class)
@IgorComponent
public class FtpFileService extends BaseFtpFileService {

    /**
     * Creates a new component instance.
     */
    public FtpFileService() {
        super("b0b068a5-2f4f-46e5-8098-464e323b538b");
    }

    /**
     * Initializes the FTP client.
     */
    protected FTPClient connect() {
        try {
            FTPClient ftpClient = new FTPClient();
            configureAndConnect(ftpClient);
            return ftpClient;
        } catch (IOException e) {
            throw new ServiceException("Could not login to FTP server " + getHost() + ":" + getPort(), e);
        }
    }

}
