package com.arassec.igor.module.file.connector.ftp;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.util.IgorException;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

/**
 * File-Connector that uses FTP as protocol.
 */
@IgorComponent
public class FtpFileConnector extends BaseFtpFileConnector {

    /**
     * Creates a new component instance.
     */
    public FtpFileConnector() {
        super("ftp-file-connector");
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
            throw new IgorException("Could not login to FTP server " + getHost() + ":" + getPort(), e);
        }
    }

}
