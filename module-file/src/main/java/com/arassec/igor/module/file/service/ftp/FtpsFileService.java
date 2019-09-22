package com.arassec.igor.module.file.service.ftp;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.service.ServiceException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.IOException;

/**
 * File-Service for FTPS servers.
 */
@IgorComponent("FTPS")
public class FtpsFileService extends FtpFileService {

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
            throw new ServiceException("Could not login to FTPS server " + getHost() + ":" + getPort(), e);
        }
    }

}
