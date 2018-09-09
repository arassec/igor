package com.arassec.igor.module.ftp.service;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.core.model.service.file.BaseFileService;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.IOException;

/**
 * File service for FTPS servers.
 */
@IgorService(label = "FTPS")
public class FtpsFileService extends FtpFileService {

    /**
     * {@inheritDoc}
     */
    @Override
    protected FTPClient connect() {
        try {
            FTPClient ftpsClient = new FTPSClient();
            ftpsClient.connect(host, port);
            if (passiveMode) {
                ftpsClient.enterLocalPassiveMode();
            } else {
                ftpsClient.enterLocalActiveMode();
            }
            String user = username;
            String pass = password;
            if (user == null || pass == null) {
                user = "anonymous";
                pass = "igor";
            }
            if (!ftpsClient.login(user, pass)) {
                throw new ServiceException("Login to FTPS server " + host + ":" + port + " failed for user: " + user);
            }
            return ftpsClient;
        } catch (IOException e) {
            throw new ServiceException("Could not login to FTPS server " + host + ":" + port, e);
        }
    }

}
