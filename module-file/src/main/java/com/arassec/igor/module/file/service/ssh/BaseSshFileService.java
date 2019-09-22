package com.arassec.igor.module.file.service.ssh;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.module.file.service.BaseFileService;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Base for SSH based File-Services (like SFTP and SCP).
 */
public abstract class BaseSshFileService extends BaseFileService {

    /**
     * Enables or disables strict host-key checking.
     */
    @IgorParam(optional = true)
    private boolean strictHostkeyChecking = false;

    /**
     * Preferred authentications.
     */
    @IgorParam(optional = true)
    private String preferredAuthentications = "publickey,keyboard-interactive,password";

    /**
     * Connection timeout.
     */
    @IgorParam(optional = true)
    private int timeout = 30000;

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
            session.setConfig("StrictHostKeyChecking", strictHostkeyChecking ? "yes" : "no");
            session.setConfig("PreferredAuthentications", preferredAuthentications);
            session.connect(timeout);
            return session;
        } catch (JSchException e) {
            throw new ServiceException("Could not connect to server " + host + ":" + port, e);
        }
    }

}
