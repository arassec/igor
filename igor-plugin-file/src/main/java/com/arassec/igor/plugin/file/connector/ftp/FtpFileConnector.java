package com.arassec.igor.plugin.file.connector.ftp;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.file.FileType;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

/**
 * <h2>FTP Connector</h2>
 *
 * <h3>Description</h3>
 * A file-connector providing access to an FTP server.
 */
@IgorComponent(categoryId = CoreCategory.FILE, typeId = FileType.FTP_CONNECTOR)
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
