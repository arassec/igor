package com.arassec.igor.module.file.connector.ftp;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.util.IgorException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.io.IOException;

/**
 * File-Connector for FTPS servers.
 */
@ConditionalOnClass(FTPSClient.class)
@IgorComponent
public class FtpsFileConnector extends BaseFtpFileConnector {

    /**
     * Creates a new component instance.
     */
    public FtpsFileConnector() {
        super("ftps-file-connector");
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
