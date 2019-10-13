package com.arassec.igor.module.file.service.ftp;

import com.arassec.igor.core.model.service.ServiceException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * File-Service for FTPS servers.
 */
@Component
@Scope("prototype")
@ConditionalOnClass(FTPSClient.class)
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeId() {
        return "034a943b-1536-4ba9-8fd4-b1bc9880475e";
    }

}
