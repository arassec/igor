package com.arassec.igor.module.file.service.ftp;

import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.module.file.service.FileService;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.IOException;

/**
 * {@link FileService} for FTPS servers.
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
            ftpsClient.setStrictReplyParsing(false);

            if (windowsFtp) {
                ftpsClient.configure(new FTPClientConfig(FTPClientConfig.SYST_NT));
            } else {
                ftpsClient.configure(new FTPClientConfig());
            }

            ftpsClient.setBufferSize(bufferSize);

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

            ftpsClient.setDataTimeout(dataTimeout);
            ftpsClient.setControlKeepAliveTimeout(keepAliveTimeout);
            ftpsClient.setControlKeepAliveReplyTimeout(keepAliveTimeout);
            ftpsClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            ftpsClient.setFileType(FTP.BINARY_FILE_TYPE);

            ((FTPSClient) ftpsClient).execPBSZ(0);
            ((FTPSClient) ftpsClient).execPROT("P");

            return ftpsClient;
        } catch (IOException e) {
            throw new ServiceException("Could not login to FTPS server " + host + ":" + port, e);
        }
    }

}
