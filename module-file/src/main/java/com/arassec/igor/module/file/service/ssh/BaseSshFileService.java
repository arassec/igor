package com.arassec.igor.module.file.service.ssh;

import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.module.file.service.BaseFileService;
import com.arassec.igor.module.file.service.FileService;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Base for SSH based {@link FileService}s (like SFTP and SCP).
 */
public abstract class BaseSshFileService extends BaseFileService {

    /**
     * Initializes the SSH session.
     *
     * @return A new SSH session.
     */
    protected Session connect(String host, int port, String username, String password) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, host, port);
            session.setPassword(password);
            // TODO: Make these IgorParams.
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
            session.connect(30000);
            return session;
        } catch (JSchException e) {
            throw new ServiceException("Could not connect to server " + host + ":" + port, e);
        }
    }

}
