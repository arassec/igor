package com.arassec.igor.plugin.file.connector.ftp;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.file.FilePluginType;
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
        super(FilePluginType.FTP_FILE_CONNECTOR.getId());
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
