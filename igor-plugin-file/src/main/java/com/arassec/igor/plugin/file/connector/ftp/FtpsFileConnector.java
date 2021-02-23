package com.arassec.igor.plugin.file.connector.ftp;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.file.FilePluginType;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.IOException;

/**
 * File-Connector for FTPS servers.
 */
@IgorComponent
public class FtpsFileConnector extends BaseFtpFileConnector {

    /**
     * Creates a new component instance.
     */
    public FtpsFileConnector() {
        super(FilePluginType.FTPS_FILE_CONNECTOR.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FTPClient connect() {
        try {
            FTPSClient ftpsClient = new FTPSClient();
            configureAndConnect(ftpsClient);

            ftpsClient.execPBSZ(0);
            ftpsClient.execPROT("P");

            return ftpsClient;
        } catch (IOException e) {
            throw new IgorException("Could not login to FTPS server " + getHost() + ":" + getPort(), e);
        }
    }

}
