package com.arassec.igor.module.file.service.ssh;

import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.module.file.service.BaseFileService;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Base for SSH based file services (like SFTP and SCP).
 * <p>
 * Created by Andreas Sensen on 17.04.2017.
 */
public abstract class BaseSshFileService extends BaseFileService {

    /**
     * Initializes the SSH session.
     *
     * @return A new SSH session.
     */
    protected Session connect(String host, int port, String username, String password) {
        try {
            Session session = (new JSch()).getSession(username, host, port);
            session.setPassword(password);
            // TODO: Config!
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(30000);
            return session;
        } catch (JSchException e) {
            throw new ServiceException("Could not connect to server " + host + ":" + port + ".", e);
        }
    }

}
