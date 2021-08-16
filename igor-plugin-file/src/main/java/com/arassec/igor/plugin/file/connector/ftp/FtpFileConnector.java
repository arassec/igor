package com.arassec.igor.plugin.file.connector.ftp;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.CoreCategory;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

/**
 * <h1>FTP Connector</h1>
 *
 * <h2>Description</h2>
 * A file-connector providing access to an FTP server.
 */
@IgorComponent(typeId = "ftp-file-connector", categoryId = CoreCategory.FILE)
public class FtpFileConnector extends BaseFtpFileConnector {

    /**
     * Initializes the FTP client.
     */
    protected FTPClient connect() {
        try {
            var ftpClient = new FTPClient();
            configureAndConnect(ftpClient);
            return ftpClient;
        } catch (IOException e) {
            throw new IgorException("Could not login to FTP server " + getHost() + ":" + getPort(), e);
        }
    }

}
