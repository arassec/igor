package com.arassec.igor.plugin.file.connector.ftp;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.CoreCategory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.IOException;

/**
 * <h2>FTPS Connector</h2>
 *
 * <h3>Description</h3>
 * A file-connector providing access to an FTPS server.
 */
@IgorComponent(typeId = "ftps-file-connector", categoryId = CoreCategory.FILE)
public class FtpsFileConnector extends BaseFtpFileConnector {

    /**
     * {@inheritDoc}
     */
    @Override
    protected FTPClient connect() {
        try {
            var ftpsClient = new FTPSClient();
            configureAndConnect(ftpsClient);

            ftpsClient.execPBSZ(0);
            ftpsClient.execPROT("P");

            return ftpsClient;
        } catch (IOException e) {
            throw new IgorException("Could not login to FTPS server " + getHost() + ":" + getPort(), e);
        }
    }

}
