package com.arassec.igor.module.file.service.ftp;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.service.ServiceException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.io.IOException;

/**
 * File-Service for FTPS servers.
 */
@ConditionalOnClass(FTPSClient.class)
@IgorComponent
public class FtpsFileService extends BaseFtpFileService {

    /**
     * Creates a new component instance.
     */
    public FtpsFileService() {
        super("034a943b-1536-4ba9-8fd4-b1bc9880475e");
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
            throw new ServiceException("Could not login to FTPS server " + getHost() + ":" + getPort(), e);
        }
    }

}
